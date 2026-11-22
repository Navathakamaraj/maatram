package org.maatram.service;

import java.util.HashMap;
import java.util.Map;

public class CompanyNormalizer {
    private static final Map<String, String> COMPANY_ALIASES = new HashMap<>();

    static {
        COMPANY_ALIASES.put("tcs", "tata consultancy services");
        COMPANY_ALIASES.put("zoho corp", "zoho corporation");
        COMPANY_ALIASES.put("l&t", "larsen and toubro");
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String n = name.toLowerCase().trim();
        n = n.replaceAll("[.,]", " ");
        n = n.replaceAll("\\b(pvt|private|ltd|limited|inc|llp)\\b", " ");
        n = n.replaceAll("\\s+", " ").trim();
        if (COMPANY_ALIASES.containsKey(n)) {
            n = COMPANY_ALIASES.get(n);
        }
        return n;
    }
}
