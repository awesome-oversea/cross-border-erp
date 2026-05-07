package com.aidotnet.erp.tms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.tms.client.FmsClient;
import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.CarrierRecommendation;
import com.aidotnet.erp.tms.domain.Shipment;
import com.aidotnet.erp.tms.domain.ShipmentStatus;
import com.aidotnet.erp.tms.domain.ShippingCost;
import com.aidotnet.erp.tms.domain.ShippingMethod;
import com.aidotnet.erp.tms.domain.ShippingRate;
import com.aidotnet.erp.tms.domain.TrackingEvent;
import com.aidotnet.erp.tms.infrastructure.ShipmentStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 鐗╂祦鍙戣揣绠＄悊搴旂敤鏈嶅姟
 * <p>
 * 鎻忚堪: 鐗╂祦鍩熸牳蹇冩湇鍔★紝璐熻矗鐗╂祦鍟嗙鐞嗐€佸彂璐у崟绠＄悊銆佺墿娴佽建杩硅拷韪€? *       杩愯垂鎴愭湰璁板綍銆佺墿娴佸晢鏅鸿兘鎺ㄨ崘绛変笟鍔￠€昏緫銆? *       鏄繛鎺ヨ鍗曞煙(OMS)銆佷粨鍌ㄥ煙(WMS)銆佽储鍔″煙(FMS)鐨勭墿娴佽皟搴︿腑蹇冦€? * </p>
 * <p>
 * 鏍稿績鑳藉姏:
 *   1. 鐗╂祦鍟嗙鐞?- 鍒涘缓鐗╂祦鍟嗭紝缁存姢鑱旂郴鏂瑰紡鍜孉PI鍚敤鐘舵€?
 *   2. 鍙戣揣鍗曠鐞?- 鍒涘缓鍙戣揣鍗曪紝鍏宠仈璁㈠崟/浠撳簱/鐗╂祦鍟?杩愯緭鏂瑰紡
 *   3. 鐗╂祦杞ㄨ抗 - 娣诲姞鐗╂祦杞ㄨ抗浜嬩欢锛岃嚜鍔ㄦ洿鏂板彂璐у崟鐘舵€?
 *   4. 杩愯垂鎴愭湰 - 璁板綍杩愯垂/鐕冩补闄勫姞璐?鍏朵粬璐圭敤锛岃嚜鍔ㄥ悓姝MS
 *   5. 鐗╂祦鍟嗘帹鑽?- 鏍规嵁鐩殑鍦?閲嶉噺/閲戦鏅鸿兘鎺ㄨ崘鏈€浼樼墿娴佸晢
 * </p>
 * <p>
 * 涓氬姟瑙勫垯:
 *   1. 鐗╂祦璺熻釜鍙峰敮涓€鎬ф牎楠?
 *   2. 宸茬鏀?宸插彇娑堝彂璐у崟涓嶅彲娣诲姞杞ㄨ抗
 *   3. DELIVERED鐘舵€佽建杩硅嚜鍔ㄥ皢鍙戣揣鍗曟爣璁颁负宸茬鏀?
 *   4. 杩愯垂鎴愭湰鑷姩璁板綍鍒癋MS鎴愭湰浜嬩欢
 *   5. 鐗╂祦鍟嗘帹鑽愭牴鎹揩閫?鏍囧噯/缁忔祹涓夋。鍒嗙被锛岀患鍚堣瘎鍒嗘帓搴?
 * </p>
 *
 * @author ERP绯荤粺
 * @see Shipment
 * @see Carrier
 * @see ShipmentStore
 */
@Service
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentStore shipmentStore;
    private final FmsClient fmsClient;
    private final LogisticsStrategyService logisticsStrategyService;

    public ShipmentService(ShipmentStore shipmentStore,
                           FmsClient fmsClient,
                           LogisticsStrategyService logisticsStrategyService) {
        this.shipmentStore = shipmentStore;
        this.fmsClient = fmsClient;
        this.logisticsStrategyService = logisticsStrategyService;
    }

    @Transactional
    public Carrier createCarrier(String tenantId, CreateCarrierCommand command) {
        String code = normalizeCode(command.code());
        ensureCarrierCodeAvailable(tenantId, code, null);
        Instant now = Instant.now();
        String authorizationStatus = upperOrDefault(command.authorizationStatus(), Carrier.AuthorizationStatus.PENDING.name());
        Instant lastAuthorizedAt = command.lastAuthorizedAt();
        if (Carrier.AuthorizationStatus.AUTHORIZED.name().equals(authorizationStatus) && lastAuthorizedAt == null) {
            lastAuthorizedAt = now;
        }
        return shipmentStore.saveCarrier(new Carrier(
                UUID.randomUUID().toString(),
                tenantId,
                code,
                command.name(),
                upperOrNull(command.countryCode()),
                upperOrDefault(command.type(), Carrier.CarrierType.STANDARD.name()),
                upperOrDefault(command.status(), Carrier.CarrierStatus.ACTIVE.name()),
                command.contactPerson(),
                command.phone(),
                command.apiEnabled(),
                command.featured(),
                authorizationStatus,
                command.authorizationValidUntil(),
                lastAuthorizedAt,
                now,
                now));
    }

    @Transactional
    public Carrier updateCarrier(String tenantId, String carrierId, UpdateCarrierCommand command) {
        Carrier existing = getCarrier(tenantId, carrierId);
        String nextCode = normalizeCode(command.code() != null ? command.code() : existing.code());
        ensureCarrierCodeAvailable(tenantId, nextCode, carrierId);
        Instant now = Instant.now();
        String authorizationStatus = command.authorizationStatus() != null
                ? upperOrDefault(command.authorizationStatus(), Carrier.AuthorizationStatus.PENDING.name())
                : existing.authorizationStatus();
        Instant lastAuthorizedAt = command.lastAuthorizedAt() != null ? command.lastAuthorizedAt() : existing.lastAuthorizedAt();
        if (Carrier.AuthorizationStatus.AUTHORIZED.name().equals(authorizationStatus) && lastAuthorizedAt == null) {
            lastAuthorizedAt = now;
        }
        return shipmentStore.saveCarrier(new Carrier(
                existing.carrierId(),
                existing.tenantId(),
                nextCode,
                chooseText(command.name(), existing.name()),
                upperOrFallback(command.countryCode(), existing.countryCode()),
                upperOrFallback(command.type(), existing.type()),
                upperOrFallback(command.status(), existing.status()),
                chooseText(command.contactPerson(), existing.contactPerson()),
                chooseText(command.phone(), existing.phone()),
                command.apiEnabled() != null ? command.apiEnabled() : existing.apiEnabled(),
                command.featured() != null ? command.featured() : existing.featured(),
                authorizationStatus,
                command.authorizationValidUntil() != null ? command.authorizationValidUntil() : existing.authorizationValidUntil(),
                lastAuthorizedAt,
                existing.createdAt(),
                now));
    }

    public Carrier getCarrier(String tenantId, String carrierId) {
        return shipmentStore.findCarrier(tenantId, carrierId)
                .orElseThrow(() -> new BizException("CARRIER_NOT_FOUND", "Carrier does not exist"));
    }

    public List<Carrier> listCarriers(String tenantId) {
        return shipmentStore.listCarriers(tenantId);
    }

    public List<ShippingMethod> listShippingMethods(String tenantId) {
        return shipmentStore.listShippingMethods(tenantId);
    }

    @Transactional
    public ShippingMethod createShippingMethod(String tenantId, String carrierId, CreateShippingMethodCommand command) {
        getCarrier(tenantId, carrierId);
        String methodCode = normalizeCode(command.methodCode());
        ensureShippingMethodCodeAvailable(tenantId, carrierId, methodCode);
        validateEstimatedDays(command.estimatedDaysMin(), command.estimatedDaysMax());
        Instant now = Instant.now();
        return shipmentStore.saveShippingMethod(new ShippingMethod(
                UUID.randomUUID().toString(),
                tenantId,
                carrierId,
                methodCode,
                command.methodName(),
                upperOrDefault(command.transportMode(), ShippingMethod.TransportMode.AIR.name()),
                upperOrDefault(command.rateType(), ShippingMethod.RateType.WEIGHT_BASED.name()),
                command.enabled(),
                command.estimatedDaysMin(),
                command.estimatedDaysMax(),
                now,
                now));
    }

    public List<ShippingMethod> listShippingMethodsByCarrier(String tenantId, String carrierId) {
        getCarrier(tenantId, carrierId);
        return shipmentStore.listShippingMethodsByCarrier(tenantId, carrierId);
    }

    @Transactional
    public ShippingRate createChannelRule(String tenantId, String carrierId, String methodId, CreateChannelRuleCommand command) {
        ShippingMethod shippingMethod = getShippingMethod(tenantId, methodId);
        if (!carrierId.equals(shippingMethod.carrierId())) {
            throw new BizException("CARRIER_METHOD_MISMATCH", "Shipping method does not belong to carrier");
        }
        validateChannelRule(command);
        Instant now = Instant.now();
        return shipmentStore.saveShippingRate(new ShippingRate(
                UUID.randomUUID().toString(),
                tenantId,
                methodId,
                upperOrNull(command.originCountry()),
                upperOrNull(command.destinationCountry()),
                upperOrNull(command.zoneCode()),
                command.weightMinKg(),
                command.weightMaxKg(),
                nonNullAmount(command.baseCost()),
                nonNullAmount(command.costPerKg()),
                upperOrDefault(command.currency(), "CNY"),
                command.effectiveFrom(),
                command.effectiveTo(),
                now,
                now));
    }

    public List<ShippingRate> listChannelRules(String tenantId, String carrierId, String methodId) {
        ShippingMethod shippingMethod = getShippingMethod(tenantId, methodId);
        if (!carrierId.equals(shippingMethod.carrierId())) {
            throw new BizException("CARRIER_METHOD_MISMATCH", "Shipping method does not belong to carrier");
        }
        return shipmentStore.listShippingRatesByMethod(tenantId, methodId);
    }

    /**
     * 跨域读取物流方式和费率时，统一由 TMS 领域主控接口返回，避免调用方直接依赖中台内部表语义。
     */
    public List<ShippingRate> listShippingRates(String tenantId,
                                                String carrierId,
                                                String shippingMethodId,
                                                String originCountry,
                                                String destinationCountry) {
        List<ShippingRate> rates;
        if (shippingMethodId != null && !shippingMethodId.isBlank()) {
            ShippingMethod shippingMethod = getShippingMethod(tenantId, shippingMethodId);
            if (carrierId != null && !carrierId.isBlank()
                    && !carrierId.equals(shippingMethod.carrierId())) {
                throw new BizException("CARRIER_METHOD_MISMATCH", "Shipping method does not belong to carrier");
            }
            rates = shipmentStore.listShippingRatesByMethod(tenantId, shippingMethodId);
        } else if ((originCountry != null && !originCountry.isBlank())
                || (destinationCountry != null && !destinationCountry.isBlank())) {
            rates = shipmentStore.listShippingRatesByRoute(
                    tenantId,
                    upperOrNull(originCountry),
                    upperOrNull(destinationCountry));
        } else {
            rates = shipmentStore.listShippingRates(tenantId);
        }

        if (carrierId == null || carrierId.isBlank()) {
            return rates;
        }
        List<String> carrierMethodIds = shipmentStore.listShippingMethodsByCarrier(tenantId, carrierId).stream()
                .map(ShippingMethod::methodId)
                .toList();
        return rates.stream()
                .filter(rate -> carrierMethodIds.contains(rate.methodId()))
                .toList();
    }

    public FreightEstimate estimateShippingRate(String tenantId, EstimateShippingRateCommand command) {
        validateEstimateCommand(tenantId, command);
        LogisticsStrategyService.FreightQuote quote = logisticsStrategyService.estimateFreightQuote(
                tenantId,
                new LogisticsStrategyService.FreightEstimateCommand(
                        command.carrierId(),
                        command.shippingMethodId(),
                        upperOrNull(command.originCountry()),
                        upperOrNull(command.destinationCountry()),
                        command.weight(),
                        command.volume()));
        if (quote == null) {
            throw new BizException("SHIPPING_RATE_NOT_FOUND", "No matching shipping rate found");
        }
        return toFreightEstimate(quote);
    }

    @Transactional
    public Shipment createShipment(String tenantId, CreateShipmentCommand command) {
        shipmentStore.findCarrier(tenantId, command.carrierId())
                .orElseThrow(() -> new BizException("CARRIER_NOT_FOUND", "Carrier does not exist"));
        if (command.shippingMethodId() != null && !command.shippingMethodId().isBlank()) {
            ShippingMethod shippingMethod = getShippingMethod(tenantId, command.shippingMethodId());
            if (!command.carrierId().equals(shippingMethod.carrierId())) {
                throw new BizException("CARRIER_METHOD_MISMATCH", "Shipping method does not belong to carrier");
            }
        }
        shipmentStore.findByTrackingNo(tenantId, command.trackingNo()).ifPresent(existing -> {
            throw new BizException("TRACKING_NO_DUPLICATED", "Tracking number already exists");
        });
        Instant now = Instant.now();
        FreightEstimate freightEstimate = tryEstimateShipmentFreight(
                tenantId,
                command.carrierId(),
                command.shippingMethodId(),
                null,
                command.destinationCountry(),
                command.weight(),
                resolveVolume(command.length(), command.width(), command.height()));
        return shipmentStore.saveShipment(new Shipment(
                UUID.randomUUID().toString(),
                tenantId,
                command.orderId(),
                command.warehouseId(),
                command.carrierId(),
                command.shippingMethodId(),
                command.trackingNo(),
                command.destinationCountry(),
                command.weight(),
                command.length(),
                command.width(),
                command.height(),
                command.estimatedDelivery(),
                null,
                freightEstimate != null ? freightEstimate.estimatedFreight() : null,
                freightEstimate != null ? freightEstimate.currency() : null,
                freightEstimate != null ? freightEstimate.chargeableWeight() : null,
                freightEstimate != null ? now : null,
                ShipmentStatus.CREATED,
                List.of(),
                now,
                now));
    }

    @Transactional
    public Shipment addTracking(String tenantId, String shipmentId, AddTrackingCommand command) {
        Shipment shipment = getShipment(tenantId, shipmentId);
        if (shipment.status() == ShipmentStatus.CANCELLED || shipment.status() == ShipmentStatus.DELIVERED) {
            throw new BizException("SHIPMENT_STATUS_INVALID", "Shipment status does not allow tracking updates");
        }
        List<TrackingEvent> events = new ArrayList<>(shipment.trackingEvents());
        events.add(new TrackingEvent(
                UUID.randomUUID().toString(),
                command.status(),
                command.location(),
                command.description(),
                Instant.now()));
        ShipmentStatus status = "DELIVERED".equalsIgnoreCase(command.status())
                ? ShipmentStatus.DELIVERED
                : ShipmentStatus.IN_TRANSIT;
        return update(shipment, status, events);
    }

    @Transactional
    public Shipment cancel(String tenantId, String shipmentId) {
        Shipment shipment = getShipment(tenantId, shipmentId);
        if (shipment.status() == ShipmentStatus.DELIVERED) {
            throw new BizException("SHIPMENT_STATUS_INVALID", "Delivered shipment cannot be cancelled");
        }
        return update(shipment, ShipmentStatus.CANCELLED, shipment.trackingEvents());
    }

    public List<Shipment> listShipments(String tenantId) {
        return shipmentStore.listShipments(tenantId);
    }

    @Transactional
    public ShippingCost recordShippingCost(String tenantId, RecordShippingCostCommand command) {
        Shipment shipment = getShipment(tenantId, command.shipmentId());
        if (!shipment.carrierId().equals(command.carrierId())) {
            throw new BizException("CARRIER_SHIPMENT_MISMATCH", "Shipping cost carrier does not match shipment");
        }
        if (shipment.estimatedFreightCurrency() != null
                && command.currency() != null
                && !shipment.estimatedFreightCurrency().equalsIgnoreCase(command.currency())) {
            throw new BizException("FREIGHT_CURRENCY_MISMATCH", "Actual freight currency must match estimated freight currency");
        }
        BigDecimal freightCost = nonNullAmount(command.freightCost());
        BigDecimal fuelSurcharge = nonNullAmount(command.fuelSurcharge());
        BigDecimal otherFees = nonNullAmount(command.otherFees());
        BigDecimal total = freightCost.add(fuelSurcharge).add(otherFees);
        ShippingCost cost = shipmentStore.saveShippingCost(new ShippingCost(
                UUID.randomUUID().toString(),
                tenantId,
                command.shipmentId(),
                command.carrierId(),
                freightCost,
                fuelSurcharge,
                otherFees,
                total,
                command.currency(),
                Instant.now()));
        recordShippingCostEvent(shipment, cost);
        return cost;
    }

    public List<ShippingCost> listShippingCosts(String tenantId) {
        return shipmentStore.listShippingCosts(tenantId);
    }

    public List<ShippingCost> listShippingCostsByCarrier(String tenantId, String carrierId) {
        return shipmentStore.listShippingCostsByCarrier(tenantId, carrierId);
    }

    public FreightDifference getFreightDifference(String tenantId, String shipmentId) {
        Shipment shipment = getShipment(tenantId, shipmentId);
        ShippingCost latestShippingCost = shipmentStore.findLatestShippingCostByShipment(tenantId, shipmentId).orElse(null);
        BigDecimal actualFreight = latestShippingCost != null ? latestShippingCost.totalCost() : null;
        BigDecimal freightDifference = shipment.estimatedFreight() != null && actualFreight != null
                ? actualFreight.subtract(shipment.estimatedFreight()).setScale(2, RoundingMode.HALF_UP)
                : null;
        String currency = latestShippingCost != null ? latestShippingCost.currency() : shipment.estimatedFreightCurrency();
        return new FreightDifference(
                shipment.shipmentId(),
                shipment.carrierId(),
                shipment.shippingMethodId(),
                shipment.status().name(),
                shipment.estimatedFreight(),
                actualFreight,
                freightDifference,
                currency,
                shipment.estimatedFreightCurrency(),
                latestShippingCost != null ? latestShippingCost.currency() : null,
                shipment.estimatedChargeableWeight(),
                shipment.estimatedAt());
    }

    public List<FreightDifference> listFreightDifferences(String tenantId, List<String> shipmentIds) {
        return shipmentIds.stream()
                .distinct()
                .map(shipmentId -> getFreightDifference(tenantId, shipmentId))
                .toList();
    }

    public Shipment getShipment(String tenantId, String shipmentId) {
        return shipmentStore.findShipment(tenantId, shipmentId)
                .orElseThrow(() -> new BizException("SHIPMENT_NOT_FOUND", "Shipment does not exist"));
    }

    public List<CarrierRecommendation> recommendCarriers(String tenantId, RecommendCarrierCommand command) {
        if (command.destinationCountry() == null || command.destinationCountry().isBlank()) {
            throw new BizException("DESTINATION_COUNTRY_REQUIRED", "Destination country is required");
        }
        if (command.warehouseCountry() == null || command.warehouseCountry().isBlank()) {
            throw new BizException("WAREHOUSE_COUNTRY_REQUIRED", "Warehouse country is required");
        }
        return shipmentStore.listCarriers(tenantId).stream()
                .filter(Carrier::isActive)
                .map(carrier -> toRecommendation(carrier, command))
                .sorted(recommendationComparator(command))
                .toList();
    }

    private ShippingMethod getShippingMethod(String tenantId, String methodId) {
        return shipmentStore.findShippingMethod(tenantId, methodId)
                .orElseThrow(() -> new BizException("SHIPPING_METHOD_NOT_FOUND", "Shipping method does not exist"));
    }

    private void validateEstimateCommand(String tenantId, EstimateShippingRateCommand command) {
        if (command.destinationCountry() == null || command.destinationCountry().isBlank()) {
            throw new BizException("DESTINATION_COUNTRY_REQUIRED", "Destination country is required");
        }
        if (command.shippingMethodId() != null && !command.shippingMethodId().isBlank()) {
            ShippingMethod method = getShippingMethod(tenantId, command.shippingMethodId());
            if (command.carrierId() != null && !command.carrierId().isBlank()
                    && !command.carrierId().equals(method.carrierId())) {
                throw new BizException("CARRIER_METHOD_MISMATCH", "Shipping method does not belong to carrier");
            }
        } else if (command.carrierId() != null && !command.carrierId().isBlank()) {
            getCarrier(tenantId, command.carrierId());
        }
    }

    /**
     * 运单创建时固化一次估算结果，后续即使费率表调整，差异分析仍以发货当时口径为准。
     */
    private FreightEstimate tryEstimateShipmentFreight(String tenantId,
                                                       String carrierId,
                                                       String shippingMethodId,
                                                       String originCountry,
                                                       String destinationCountry,
                                                       BigDecimal weight,
                                                       BigDecimal volume) {
        LogisticsStrategyService.FreightQuote quote = logisticsStrategyService.estimateFreightQuote(
                tenantId,
                new LogisticsStrategyService.FreightEstimateCommand(
                        carrierId,
                        shippingMethodId,
                        upperOrNull(originCountry),
                        upperOrNull(destinationCountry),
                        weight,
                        volume));
        return quote != null ? toFreightEstimate(quote) : null;
    }

    private FreightEstimate toFreightEstimate(LogisticsStrategyService.FreightQuote quote) {
        return new FreightEstimate(
                quote.carrierId(),
                quote.shippingMethodId(),
                quote.zoneCode(),
                quote.chargeableWeight(),
                quote.estimatedFreight(),
                quote.currency(),
                quote.estimatedDaysMin(),
                quote.estimatedDaysMax());
    }

    /**
     * 授权状态只保留业务可见元数据，不在此处存储敏感秘钥，避免把连接器密钥散落到领域表。
     */
    private void ensureCarrierCodeAvailable(String tenantId, String code, String currentCarrierId) {
        shipmentStore.findCarrierByCode(tenantId, code).ifPresent(existing -> {
            if (currentCarrierId == null || !existing.carrierId().equals(currentCarrierId)) {
                throw new BizException("CARRIER_CODE_DUPLICATED", "Carrier code already exists");
            }
        });
    }

    private void ensureShippingMethodCodeAvailable(String tenantId, String carrierId, String methodCode) {
        shipmentStore.findShippingMethodByCode(tenantId, carrierId, methodCode).ifPresent(existing -> {
            throw new BizException("SHIPPING_METHOD_CODE_DUPLICATED", "Shipping method code already exists");
        });
    }

    private void validateEstimatedDays(Integer estimatedDaysMin, Integer estimatedDaysMax) {
        if (estimatedDaysMin != null && estimatedDaysMin < 0) {
            throw new BizException("ESTIMATED_DAYS_INVALID", "Estimated minimum days must be greater than or equal to zero");
        }
        if (estimatedDaysMax != null && estimatedDaysMax < 0) {
            throw new BizException("ESTIMATED_DAYS_INVALID", "Estimated maximum days must be greater than or equal to zero");
        }
        if (estimatedDaysMin != null && estimatedDaysMax != null && estimatedDaysMin > estimatedDaysMax) {
            throw new BizException("ESTIMATED_DAYS_INVALID", "Estimated minimum days cannot exceed maximum days");
        }
    }

    /**
     * 渠道规则必须满足重量区间、有效期区间和费率非负，避免脏规则进入报价链路。
     */
    private void validateChannelRule(CreateChannelRuleCommand command) {
        if (command.weightMinKg() != null && command.weightMinKg().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("CHANNEL_RULE_INVALID", "Weight minimum must be greater than or equal to zero");
        }
        if (command.weightMaxKg() != null && command.weightMaxKg().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("CHANNEL_RULE_INVALID", "Weight maximum must be greater than or equal to zero");
        }
        if (command.weightMinKg() != null && command.weightMaxKg() != null
                && command.weightMinKg().compareTo(command.weightMaxKg()) > 0) {
            throw new BizException("CHANNEL_RULE_INVALID", "Weight minimum cannot exceed maximum");
        }
        if (command.effectiveFrom() != null && command.effectiveTo() != null
                && command.effectiveFrom().isAfter(command.effectiveTo())) {
            throw new BizException("CHANNEL_RULE_INVALID", "Effective from cannot be after effective to");
        }
    }

    private BigDecimal resolveVolume(BigDecimal length, BigDecimal width, BigDecimal height) {
        if (length == null || width == null || height == null) {
            return null;
        }
        return length.multiply(width).multiply(height);
    }

    private Shipment update(Shipment shipment, ShipmentStatus status, List<TrackingEvent> events) {
        return shipmentStore.saveShipment(new Shipment(
                shipment.shipmentId(),
                shipment.tenantId(),
                shipment.orderId(),
                shipment.warehouseId(),
                shipment.carrierId(),
                shipment.shippingMethodId(),
                shipment.trackingNo(),
                shipment.destinationCountry(),
                shipment.weight(),
                shipment.length(),
                shipment.width(),
                shipment.height(),
                shipment.estimatedDelivery(),
                shipment.actualDelivery(),
                shipment.estimatedFreight(),
                shipment.estimatedFreightCurrency(),
                shipment.estimatedChargeableWeight(),
                shipment.estimatedAt(),
                status,
                events,
                shipment.createdAt(),
                Instant.now()));
    }

    private BigDecimal nonNullAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private CarrierRecommendation toRecommendation(Carrier carrier, RecommendCarrierCommand command) {
        CarrierProfile profile = resolveProfile(carrier);
        boolean domestic = command.destinationCountry().equalsIgnoreCase(command.warehouseCountry());
        int estimatedDays = switch (profile) {
            case EXPRESS -> domestic ? 1 : 3;
            case STANDARD -> domestic ? 3 : 6;
            case ECONOMY -> domestic ? 5 : 9;
        };
        BigDecimal amount = nonNullAmount(command.packageAmount());
        BigDecimal baseCost = switch (profile) {
            case EXPRESS -> new BigDecimal("28");
            case STANDARD -> new BigDecimal("18");
            case ECONOMY -> new BigDecimal("10");
        };
        BigDecimal quantityFactor = switch (profile) {
            case EXPRESS -> new BigDecimal("3.5");
            case STANDARD -> new BigDecimal("2.2");
            case ECONOMY -> new BigDecimal("1.5");
        };
        BigDecimal amountFactor = switch (profile) {
            case EXPRESS -> new BigDecimal("0.015");
            case STANDARD -> new BigDecimal("0.010");
            case ECONOMY -> new BigDecimal("0.005");
        };
        BigDecimal estimatedCost = baseCost
                .add(quantityFactor.multiply(BigDecimal.valueOf(command.packageQuantity())))
                .add(amountFactor.multiply(amount));
        if (domestic) {
            estimatedCost = estimatedCost.multiply(new BigDecimal("0.65"));
        }
        if (command.splitShipment()) {
            estimatedCost = estimatedCost.add(new BigDecimal("3.00"));
        }
        estimatedCost = estimatedCost.setScale(2, RoundingMode.HALF_UP);
        int score = switch (profile) {
            case EXPRESS -> 90;
            case STANDARD -> 75;
            case ECONOMY -> 60;
        };
        if (domestic) {
            score += 10;
        }
        if (amount.compareTo(new BigDecimal("500")) >= 0 && profile == CarrierProfile.EXPRESS) {
            score += 10;
        }
        return new CarrierRecommendation(
                carrier.carrierId(),
                carrier.code(),
                carrier.name(),
                profile.name(),
                estimatedCost,
                estimatedDays,
                score);
    }

    private Comparator<CarrierRecommendation> recommendationComparator(RecommendCarrierCommand command) {
        BigDecimal amount = nonNullAmount(command.packageAmount());
        if (amount.compareTo(new BigDecimal("500")) >= 0) {
            return Comparator.comparingInt(CarrierRecommendation::estimatedDeliveryDays)
                    .thenComparing(CarrierRecommendation::estimatedCost);
        }
        return Comparator.comparing(CarrierRecommendation::estimatedCost)
                .thenComparingInt(CarrierRecommendation::estimatedDeliveryDays);
    }

    private CarrierProfile resolveProfile(Carrier carrier) {
        String fingerprint = (carrier.code() + " " + carrier.name()).toUpperCase(Locale.ROOT);
        if (fingerprint.contains("DHL")
                || fingerprint.contains("UPS")
                || fingerprint.contains("FEDEX")
                || fingerprint.contains("EXPRESS")
                || fingerprint.contains("AIR")) {
            return CarrierProfile.EXPRESS;
        }
        if (fingerprint.contains("POSTAL")
                || fingerprint.contains("ECONOMY")
                || fingerprint.contains("SEA")
                || fingerprint.contains("MAIL")) {
            return CarrierProfile.ECONOMY;
        }
        return CarrierProfile.STANDARD;
    }

    private void recordShippingCostEvent(Shipment shipment, ShippingCost cost) {
        try {
            fmsClient.recordCostEvent(new FmsClient.RecordCostEventRequest(
                    "SHIPPING_COST",
                    "TMS_SHIPMENT",
                    shipment.shipmentId(),
                    null,
                    null,
                    cost.currency(),
                    cost.totalCost(),
                    cost.createdAt()));
        } catch (Exception ex) {
            log.warn("Record shipping cost event failed. shipmentId={}", shipment.shipmentId(), ex);
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return code;
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String upperOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String upperOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank()
                ? defaultValue
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private String upperOrFallback(String value, String fallback) {
        return value == null || value.isBlank()
                ? fallback
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private String chooseText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record CreateCarrierCommand(String code, String name, String countryCode, String type,
                                       String contactPerson, String phone, String status,
                                       boolean apiEnabled, boolean featured, String authorizationStatus,
                                       Instant authorizationValidUntil, Instant lastAuthorizedAt) {}

    public record UpdateCarrierCommand(String code, String name, String countryCode, String type,
                                       String status, String contactPerson, String phone,
                                       Boolean apiEnabled, Boolean featured, String authorizationStatus,
                                       Instant authorizationValidUntil, Instant lastAuthorizedAt) {}

    public record CreateShippingMethodCommand(String methodCode, String methodName, String transportMode,
                                              String rateType, boolean enabled, Integer estimatedDaysMin,
                                              Integer estimatedDaysMax) {}

    public record CreateChannelRuleCommand(String originCountry, String destinationCountry, String zoneCode,
                                           BigDecimal weightMinKg, BigDecimal weightMaxKg,
                                           BigDecimal baseCost, BigDecimal costPerKg, String currency,
                                           Instant effectiveFrom, Instant effectiveTo) {}

    public record CreateShipmentCommand(String orderId, String warehouseId, String carrierId, String shippingMethodId,
                                        String trackingNo, String destinationCountry,
                                        BigDecimal weight, BigDecimal length, BigDecimal width, BigDecimal height,
                                        Instant estimatedDelivery) {}

    public record EstimateShippingRateCommand(String carrierId, String shippingMethodId, String originCountry,
                                              String destinationCountry, BigDecimal weight, BigDecimal volume) {}

    public record AddTrackingCommand(String status, String location, String description) {}

    public record RecordShippingCostCommand(String shipmentId, String carrierId, BigDecimal freightCost,
                                            BigDecimal fuelSurcharge, BigDecimal otherFees, String currency) {}

    public record FreightEstimate(String carrierId, String shippingMethodId, String zoneCode,
                                  BigDecimal chargeableWeight, BigDecimal estimatedFreight, String currency,
                                  Integer estimatedDaysMin, Integer estimatedDaysMax) {}

    public record FreightDifference(String shipmentId, String carrierId, String shippingMethodId, String status,
                                    BigDecimal estimatedFreight, BigDecimal actualFreight,
                                    BigDecimal freightDifference, String currency,
                                    String estimatedFreightCurrency, String actualFreightCurrency,
                                    BigDecimal estimatedChargeableWeight, Instant estimatedAt) {}

    public record RecommendCarrierCommand(String destinationCountry, String warehouseCountry,
                                          int packageQuantity, BigDecimal packageAmount, boolean splitShipment) {}

    private enum CarrierProfile {
        EXPRESS,
        STANDARD,
        ECONOMY
    }
}
