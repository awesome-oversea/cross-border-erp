package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.sys.domain.DataDictionary;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MasterDataInitService {

    private static final Logger log = LoggerFactory.getLogger(MasterDataInitService.class);

    private final SysExtStore extStore;

    public MasterDataInitService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public void initializeMasterData(String tenantId) {
        log.info("Initializing master data for tenant={}", tenantId);
        initCountries(tenantId);
        initCurrencies(tenantId);
        initChannels(tenantId);
        initMarkets(tenantId);
        initLanguages(tenantId);
        initUnits(tenantId);
        log.info("Master data initialization completed for tenant={}", tenantId);
    }

    private void initCountries(String tenantId) {
        String[][] countries = {
                {"US", "美国", "COUNTRY"}, {"GB", "英国", "COUNTRY"}, {"DE", "德国", "COUNTRY"},
                {"FR", "法国", "COUNTRY"}, {"JP", "日本", "COUNTRY"}, {"AU", "澳大利亚", "COUNTRY"},
                {"CA", "加拿大", "COUNTRY"}, {"IT", "意大利", "COUNTRY"}, {"ES", "西班牙", "COUNTRY"},
                {"MX", "墨西哥", "COUNTRY"}, {"BR", "巴西", "COUNTRY"}, {"IN", "印度", "COUNTRY"},
                {"AE", "阿联酋", "COUNTRY"}, {"SA", "沙特阿拉伯", "COUNTRY"}, {"SG", "新加坡", "COUNTRY"},
                {"NL", "荷兰", "COUNTRY"}, {"PL", "波兰", "COUNTRY"}, {"SE", "瑞典", "COUNTRY"},
                {"CN", "中国", "COUNTRY"}
        };
        for (int i = 0; i < countries.length; i++) {
            createDictIfNotExists(tenantId, countries[i][0], countries[i][1], countries[i][2], null, i);
        }
    }

    private void initCurrencies(String tenantId) {
        String[][] currencies = {
                {"USD", "美元", "CURRENCY"}, {"EUR", "欧元", "CURRENCY"}, {"GBP", "英镑", "CURRENCY"},
                {"JPY", "日元", "CURRENCY"}, {"AUD", "澳元", "CURRENCY"}, {"CAD", "加元", "CURRENCY"},
                {"CNY", "人民币", "CURRENCY"}, {"MXN", "墨西哥比索", "CURRENCY"}, {"BRL", "巴西雷亚尔", "CURRENCY"},
                {"INR", "印度卢比", "CURRENCY"}, {"AED", "迪拉姆", "CURRENCY"}, {"SAR", "沙特里亚尔", "CURRENCY"},
                {"SGD", "新加坡元", "CURRENCY"}, {"SEK", "瑞典克朗", "CURRENCY"}, {"PLN", "波兰兹罗提", "CURRENCY"}
        };
        for (int i = 0; i < currencies.length; i++) {
            createDictIfNotExists(tenantId, currencies[i][0], currencies[i][1], currencies[i][2], null, i);
        }
    }

    private void initChannels(String tenantId) {
        String[][] channels = {
                {"AMAZON_NA", "亚马逊北美", "CHANNEL", "AMAZON"},
                {"AMAZON_EU", "亚马逊欧洲", "CHANNEL", "AMAZON"},
                {"AMAZON_JP", "亚马逊日本", "CHANNEL", "AMAZON"},
                {"AMAZON_AU", "亚马逊澳洲", "CHANNEL", "AMAZON"},
                {"EBAY_US", "eBay美国", "CHANNEL", "EBAY"},
                {"EBAY_UK", "eBay英国", "CHANNEL", "EBAY"},
                {"EBAY_DE", "eBay德国", "CHANNEL", "EBAY"},
                {"SHOPIFY", "Shopify独立站", "CHANNEL", "SHOPIFY"},
                {"WALMART", "Walmart", "CHANNEL", "WALMART"},
                {"TEMU", "Temu", "CHANNEL", "TEMU"},
                {"SHEIN", "SHEIN", "CHANNEL", "SHEIN"},
                {"TIKTOK_SHOP", "TikTok Shop", "CHANNEL", "TIKTOK"},
                {"MERCADO_LIBRE", "Mercado Libre", "CHANNEL", "MERCADO_LIBRE"},
                {"ALIEXPRESS", "速卖通", "CHANNEL", "ALIEXPRESS"},
                {"LAZADA", "Lazada", "CHANNEL", "LAZADA"},
                {"SHOPEE", "Shopee", "CHANNEL", "SHOPEE"}
        };
        for (int i = 0; i < channels.length; i++) {
            createDictIfNotExists(tenantId, channels[i][0], channels[i][1], channels[i][2], channels[i][3], i);
        }
    }

    private void initMarkets(String tenantId) {
        String[][] markets = {
                {"NA", "北美市场", "MARKET"}, {"EU", "欧洲市场", "MARKET"},
                {"JP", "日本市场", "MARKET"}, {"AU", "澳洲市场", "MARKET"},
                {"LATAM", "拉美市场", "MARKET"}, {"MEA", "中东非洲市场", "MARKET"},
                {"SEA", "东南亚市场", "MARKET"}, {"IN", "南亚市场", "MARKET"}
        };
        for (int i = 0; i < markets.length; i++) {
            createDictIfNotExists(tenantId, markets[i][0], markets[i][1], markets[i][2], null, i);
        }
    }

    private void initLanguages(String tenantId) {
        String[][] languages = {
                {"zh-CN", "简体中文", "LANGUAGE"}, {"en-US", "美式英语", "LANGUAGE"},
                {"en-GB", "英式英语", "LANGUAGE"}, {"de-DE", "德语", "LANGUAGE"},
                {"fr-FR", "法语", "LANGUAGE"}, {"ja-JP", "日语", "LANGUAGE"},
                {"es-ES", "西班牙语", "LANGUAGE"}, {"it-IT", "意大利语", "LANGUAGE"},
                {"pt-BR", "巴西葡萄牙语", "LANGUAGE"}, {"ar-SA", "阿拉伯语", "LANGUAGE"},
                {"hi-IN", "印地语", "LANGUAGE"}, {"nl-NL", "荷兰语", "LANGUAGE"},
                {"pl-PL", "波兰语", "LANGUAGE"}, {"sv-SE", "瑞典语", "LANGUAGE"},
                {"th-TH", "泰语", "LANGUAGE"}, {"vi-VN", "越南语", "LANGUAGE"},
                {"id-ID", "印尼语", "LANGUAGE"}, {"ms-MY", "马来语", "LANGUAGE"}
        };
        for (int i = 0; i < languages.length; i++) {
            createDictIfNotExists(tenantId, languages[i][0], languages[i][1], languages[i][2], null, i);
        }
    }

    private void initUnits(String tenantId) {
        String[][] units = {
                {"PCS", "件", "UNIT"}, {"SET", "套", "UNIT"}, {"PAIR", "双", "UNIT"},
                {"KG", "千克", "UNIT"}, {"G", "克", "UNIT"}, {"LB", "磅", "UNIT"},
                {"OZ", "盎司", "UNIT"}, {"M", "米", "UNIT"}, {"CM", "厘米", "UNIT"},
                {"IN", "英寸", "UNIT"}, {"FT", "英尺", "UNIT"}, {"L", "升", "UNIT"},
                {"ML", "毫升", "UNIT"}, {"BOX", "箱", "UNIT"}, {"PACK", "包", "UNIT"},
                {"ROLL", "卷", "UNIT"}, {"BAG", "袋", "UNIT"}
        };
        for (int i = 0; i < units.length; i++) {
            createDictIfNotExists(tenantId, units[i][0], units[i][1], units[i][2], null, i);
        }
    }

    private void createDictIfNotExists(String tenantId, String code, String name, String type, String parentCode, int sortOrder) {
        extStore.findDataDictionaryByCode(tenantId, code).ifPresent(existing -> { return; });
        Instant now = Instant.now();
        DataDictionary dict = new DataDictionary(UUID.randomUUID().toString(), tenantId, code, name, type, parentCode, sortOrder, true, null, now, now);
        extStore.saveDataDictionary(dict);
    }
}
