package com.lostandfound.service;

import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.enums.CustodyStatus;
import com.lostandfound.entity.enums.FoundItemVerificationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

/** Dynamic query building for the public found-items search endpoint. */
public final class FoundItemSpecifications {

    private FoundItemSpecifications() {}

    public static Specification<FoundItem> isPubliclySearchable() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("verificationStatus"), FoundItemVerificationStatus.VERIFIED),
                cb.equal(root.get("custodyStatus"), CustodyStatus.IN_CUSTODY)
        );
    }

    public static Specification<FoundItem> hasCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category").get("categoryId"), categoryId);
    }

    public static Specification<FoundItem> hasColor(String color) {
        if (color == null || color.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("color")), color.toLowerCase());
    }

    public static Specification<FoundItem> hasBrand(String brand) {
        if (brand == null || brand.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
    }

    public static Specification<FoundItem> descriptionContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<FoundItem> cityLike(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("location").get("city")), "%" + city.toLowerCase() + "%");
    }

    public static Specification<FoundItem> foundDateFrom(LocalDate from) {
        if (from == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("foundDate"), from);
    }

    public static Specification<FoundItem> foundDateTo(LocalDate to) {
        if (to == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("foundDate"), to);
    }

    @SafeVarargs
    public static Specification<FoundItem> combine(Specification<FoundItem>... specs) {
        Specification<FoundItem> result = Specification.where(isPubliclySearchable());
        for (Specification<FoundItem> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}
