package com.aidotnet.erp.common.masking;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class DataMaskingService {

    private final Map<String, Function<String, String>> rules = new HashMap<>();

    public DataMaskingService() {
        rules.put("phone", this::maskPhone);
        rules.put("email", this::maskEmail);
        rules.put("idCard", this::maskIdCard);
        rules.put("bankCard", this::maskBankCard);
        rules.put("name", this::maskName);
        rules.put("address", this::maskAddress);
    }

    public String mask(String data, String ruleName) {
        Function<String, String> rule = rules.get(ruleName);
        if (rule == null) {
            return data;
        }
        return rule.apply(data);
    }

    public Map<String, String> maskMap(Map<String, String> data, Map<String, String> fieldRules) {
        Map<String, String> result = new HashMap<>(data);
        for (Map.Entry<String, String> entry : fieldRules.entrySet()) {
            String field = entry.getKey();
            String rule = entry.getValue();
            if (result.containsKey(field)) {
                result.put(field, mask(result.get(field), rule));
            }
        }
        return result;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) return email;
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return idCard;
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    private String maskBankCard(String card) {
        if (card == null || card.length() < 8) return card;
        return "**** **** **** " + card.substring(card.length() - 4);
    }

    private String maskName(String name) {
        if (name == null || name.length() <= 1) return name;
        return name.charAt(0) + "**";
    }

    private String maskAddress(String address) {
        if (address == null || address.length() < 6) return address;
        return address.substring(0, 3) + "****" + address.substring(address.length() - 3);
    }
}
