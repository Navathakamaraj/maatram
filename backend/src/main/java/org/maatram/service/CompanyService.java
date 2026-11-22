package org.maatram.service;

import org.maatram.exception.ApiException;
import org.maatram.model.CareerHistory;
import org.maatram.model.Company;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final DataStore dataStore;

    public CompanyService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Map<String, Object>> listCompanies() {
        Map<String, List<Company>> grouped = dataStore.companies.stream().collect(Collectors.groupingBy(Company::getNormalized_name));
        List<Map<String, Object>> res = new ArrayList<>();

        for (Map.Entry<String, List<Company>> entry : grouped.entrySet()) {
            String norm = entry.getKey();
            List<Company> variants = entry.getValue();
            Set<Long> compIds = variants.stream().map(Company::getId).collect(Collectors.toSet());

            long count = dataStore.careerHistory.stream()
                    .filter(c -> compIds.contains(c.getCompany_id()) && Boolean.TRUE.equals(c.getIs_current()))
                    .map(CareerHistory::getAlumni_id)
                    .distinct()
                    .count();

            Map<String, Object> g = new HashMap<>();
            g.put("normalized_name", norm);
            g.put("variants", variants);
            g.put("alumni_count", count);
            res.add(g);
        }

        res.sort((a, b) -> Long.compare((Long) b.get("alumni_count"), (Long) a.get("alumni_count")));
        return res;
    }

    public Map<String, Object> mergeCompanies(String norm) {
        List<Company> variants = dataStore.companies.stream().filter(c -> norm != null && norm.equals(c.getNormalized_name())).toList();
        if (variants.size() < 2) {
            throw new ApiException(409, "Nothing to merge");
        }

        Company keep = variants.get(0);
        List<Long> dropIds = variants.subList(1, variants.size()).stream().map(Company::getId).toList();

        for (CareerHistory ch : dataStore.careerHistory) {
            if (dropIds.contains(ch.getCompany_id())) {
                ch.setCompany_id(keep.getId());
            }
        }

        dataStore.companies.removeIf(c -> dropIds.contains(c.getId()));

        Map<String, Object> evData = new HashMap<>();
        evData.put("kept", keep.getCompany_name());
        evData.put("merged_ids", dropIds);
        dataStore.emit("company", keep.getId(), "COMPANIES_MERGED", evData);

        return Map.of("kept", keep, "merged", dropIds.size());
    }

    public String normalizePreview(String name) {
        return CompanyNormalizer.normalize(name);
    }
}
