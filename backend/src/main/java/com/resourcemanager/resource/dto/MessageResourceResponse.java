package com.resourcemanager.resource.dto;

import com.resourcemanager.resource.entity.MessageResource;

public record MessageResourceResponse(
        Long id,
        Integer rowNumber,
        String duplicateStatus,
        String module,
        String resourceKey,
        String fullResourceKey,
        String korean, String english, String japanese,
        String description, String registeredDate, String registeredBy,
        Integer resourceKeyCount, Integer koreanCount, Integer englishCount, Integer japaneseCount,
        Integer rowOrder
) {
    public static MessageResourceResponse from(MessageResource e, int rowNumber,
                                                String duplicateStatus, String fullResourceKey,
                                                int resourceKeyCount, int koreanCount,
                                                int englishCount, int japaneseCount) {
        boolean hasRowNumber = e.getModule() != null && !e.getModule().isBlank()
                && e.getResourceKey() != null && !e.getResourceKey().isBlank();

        return new MessageResourceResponse(
                e.getId(),
                hasRowNumber ? rowNumber : null,
                hasRowNumber ? duplicateStatus : null,
                e.getModule(), e.getResourceKey(),
                hasRowNumber ? fullResourceKey : null,
                e.getKorean(), e.getEnglish(), e.getJapanese(),
                e.getDescription(), e.getRegisteredDate(), e.getRegisteredBy(),
                hasRowNumber ? resourceKeyCount : null,
                hasRowNumber ? koreanCount : null,
                hasRowNumber ? englishCount : null,
                hasRowNumber ? japaneseCount : null,
                e.getRowOrder()
        );
    }
}
