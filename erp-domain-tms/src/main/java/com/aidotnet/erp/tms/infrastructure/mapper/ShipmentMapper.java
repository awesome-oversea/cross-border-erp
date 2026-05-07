package com.aidotnet.erp.tms.infrastructure.mapper;

import com.aidotnet.erp.tms.infrastructure.data.CarrierDO;
import com.aidotnet.erp.tms.infrastructure.data.ShipmentDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingBatchDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingCostDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingMethodDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingRateDO;
import com.aidotnet.erp.tms.infrastructure.data.TrackingEventDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * TMS鍩熷彂璐ф暟鎹甅yBatis鏄犲皠鍣? * <p>
 * 鎻忚堪: 鐗╂祦鍩熸牳蹇冩暟鎹闂眰锛屾彁渚涙壙杩愬晢銆佸彂璐у崟銆佺墿娴佽建杩广€佽繍璐规垚鏈€佺墿娴佹笭閬撱€佽繍璐硅垂鐜囥€佹壒閲忓彂璐х瓑琛ㄧ殑鏁版嵁鎿嶄綔銆? * </p>
 *
 * @author ERP绯荤粺
 */
@Mapper
public interface ShipmentMapper {

    // ---- 鎵胯繍鍟?----

    /** 鏂板鎵胯繍鍟?*/
    void insertCarrier(CarrierDO carrier);
    /** 鏇存柊鎵胯繍鍟?*/
    void updateCarrier(CarrierDO carrier);
    /** 鎸夌鎴?鎵胯繍鍟咺D鏌ヨ */
    CarrierDO selectCarrier(@Param("tenantId") String tenantId, @Param("carrierId") String carrierId);
    /** 鎸夌鎴?鍜屼唬鐮佹煡璇㈡壙杩愬晢 */
    CarrierDO selectCarrierByCode(@Param("tenantId") String tenantId, @Param("code") String code);
    /** 鎸夌鎴锋煡璇㈡壙杩愬晢鍒楄〃 */
    List<CarrierDO> selectCarriers(@Param("tenantId") String tenantId);

    // ---- 鍙戣揣鍗?----

    /** 鏂板鍙戣揣鍗?*/
    void insertShipment(ShipmentDO shipment);
    /** 鏇存柊鍙戣揣鍗?*/
    void updateShipment(ShipmentDO shipment);
    /** 鎸夌鎴?鍙戣揣鍗旾D鏌ヨ */
    ShipmentDO selectShipment(@Param("tenantId") String tenantId, @Param("shipmentId") String shipmentId);
    /** 鎸夌鎴?杩借釜鍙锋煡璇㈠彂璐у崟 */
    ShipmentDO selectShipmentByTrackingNo(@Param("tenantId") String tenantId, @Param("trackingNo") String trackingNo);
    /** 鎸夌鎴锋煡璇㈠彂璐у崟鍒楄〃 */
    List<ShipmentDO> selectShipments(@Param("tenantId") String tenantId);

    // ---- 鐗╂祦杞ㄨ抗 ----

    /** 鏂板鐗╂祦杞ㄨ抗浜嬩欢 */
    void insertTrackingEvent(TrackingEventDO event);
    /** 鎸夌鎴?鍙戣揣鍗旾D鏌ヨ杞ㄨ抗鍒楄〃 */
    List<TrackingEventDO> selectTrackingEvents(@Param("tenantId") String tenantId, @Param("shipmentId") String shipmentId);

    // ---- 杩愯垂鎴愭湰 ----

    /** 鏂板杩愯垂鎴愭湰 */
    void insertShippingCost(ShippingCostDO cost);
    /** 鎸夌鎴锋煡璇㈣繍璐规垚鏈垪琛?*/
    List<ShippingCostDO> selectShippingCosts(@Param("tenantId") String tenantId);
    /** 鎸夌鎴?鎵胯繍鍟嗘煡璇㈣繍璐规垚鏈垪琛?*/
    List<ShippingCostDO> selectShippingCostsByCarrier(@Param("tenantId") String tenantId, @Param("carrierId") String carrierId);
    ShippingCostDO selectLatestShippingCostByShipment(@Param("tenantId") String tenantId, @Param("shipmentId") String shipmentId);

    // ---- 鐗╂祦娓犻亾 ----

    /** 鏂板鐗╂祦娓犻亾 */
    void insertShippingMethod(ShippingMethodDO method);
    /** 鏇存柊鐗╂祦娓犻亾 */
    void updateShippingMethod(ShippingMethodDO method);
    /** 鎸夌鎴?娓犻亾ID鏌ヨ */
    ShippingMethodDO selectShippingMethod(@Param("tenantId") String tenantId, @Param("methodId") String methodId);
    /** 鎸夌鎴?鎵胯繍鍟嗗拰鏂规硶缂栫爜鏌ヨ */
    ShippingMethodDO selectShippingMethodByCode(@Param("tenantId") String tenantId,
                                                @Param("carrierId") String carrierId,
                                                @Param("methodCode") String methodCode);
    /** 鎸夌鎴锋煡璇㈡笭閬撳垪琛?*/
    List<ShippingMethodDO> selectShippingMethods(@Param("tenantId") String tenantId);
    /** 鎸夌鎴?鎵胯繍鍟嗘煡璇㈡笭閬撳垪琛?*/
    List<ShippingMethodDO> selectShippingMethodsByCarrier(@Param("tenantId") String tenantId, @Param("carrierId") String carrierId);

    // ---- 杩愯垂璐圭巼 ----

    /** 鏂板杩愯垂璐圭巼 */
    void insertShippingRate(ShippingRateDO rate);
    /** 鏇存柊杩愯垂璐圭巼 */
    void updateShippingRate(ShippingRateDO rate);
    /** 鎸夌鎴?璐圭巼ID鏌ヨ */
    ShippingRateDO selectShippingRate(@Param("tenantId") String tenantId, @Param("rateId") String rateId);
    /** 鎸夌鎴?娓犻亾ID鏌ヨ璐圭巼鍒楄〃 */
    List<ShippingRateDO> selectShippingRatesByMethod(@Param("tenantId") String tenantId, @Param("methodId") String methodId);
    List<ShippingRateDO> selectShippingRates(@Param("tenantId") String tenantId);
    /** 鎸夌鎴?璧峰鍦?鐩殑鍦版煡璇㈤€傜敤璐圭巼 */
    List<ShippingRateDO> selectShippingRatesByRoute(@Param("tenantId") String tenantId,
                                                    @Param("originCountry") String originCountry,
                                                    @Param("destinationCountry") String destinationCountry);

    // ---- 鎵归噺鍙戣揣 ----

    /** 鏂板鎵归噺鍙戣揣 */
    void insertShippingBatch(ShippingBatchDO batch);
    /** 鏇存柊鎵归噺鍙戣揣 */
    void updateShippingBatch(ShippingBatchDO batch);
    /** 鎸夌鎴?鎵规ID鏌ヨ */
    ShippingBatchDO selectShippingBatch(@Param("tenantId") String tenantId, @Param("batchId") String batchId);
    /** 鎸夌鎴锋煡璇㈡壒閲忓彂璐у垪琛?*/
    List<ShippingBatchDO> selectShippingBatches(@Param("tenantId") String tenantId);
}
