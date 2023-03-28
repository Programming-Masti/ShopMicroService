package com.yas.rating.viewmodel;

import java.time.ZonedDateTime;

public record ProductVm(Long id,
                            String name,
                            String slug,
                            Boolean isAllowedToOrder,
                            Boolean isPublished,
                            Boolean isFeatured,
                            Boolean isVisibleIndividually,
                            ZonedDateTime createdOn) {

}
