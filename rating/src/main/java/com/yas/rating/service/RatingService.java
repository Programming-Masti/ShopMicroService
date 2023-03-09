package com.yas.rating.service;

import com.yas.rating.exception.BadRequestException;
import com.yas.rating.exception.NotFoundException;
import com.yas.rating.model.Rating;
import com.yas.rating.repository.RatingRepository;
import com.yas.rating.utils.Constants;
import com.yas.rating.viewmodel.CustomerVm;
import com.yas.rating.viewmodel.RatingListVm;
import com.yas.rating.viewmodel.RatingPostVm;
import com.yas.rating.viewmodel.RatingVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RatingService {
    private final RatingRepository ratingRepository;
    private final ProductService productService;
    private final CustomerService customerService;

    public RatingService(RatingRepository ratingRepository, ProductService productService, CustomerService customerService) {
        this.ratingRepository = ratingRepository;
        this.productService = productService;
        this.customerService = customerService;
    }

    public RatingListVm getRatingListByProductId(Long id, int pageNo, int pageSize) {
        if (productService.getProductById(id) == null) {
            throw new NotFoundException(Constants.ERROR_CODE.PRODUCT_NOT_FOUND, id);
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").descending());
        Page<Rating> ratings = ratingRepository.findByProductId(id, pageable);

        List<RatingVm> ratingVmList = new ArrayList<>();
        for (Rating rating : ratings.getContent()) {
            ratingVmList.add(RatingVm.fromModel(rating));
        }

        return new RatingListVm(ratingVmList, ratings.getTotalElements(), ratings.getTotalPages());
    }

    public RatingVm createRating(RatingPostVm ratingPostVm) {
        if (productService.getProductById(ratingPostVm.productId()) == null) {
            throw new NotFoundException(Constants.ERROR_CODE.PRODUCT_NOT_FOUND, ratingPostVm.productId());
        }

        Rating rating = new Rating();
        rating.setRatingStar(ratingPostVm.star());
        rating.setContent(ratingPostVm.content());
        rating.setProductId(ratingPostVm.productId());

        CustomerVm customerVm = customerService.getCustomer();
        rating.setLastName(customerVm.lastName());
        rating.setFirstName(customerVm.firstName());

        Rating savedRating = ratingRepository.saveAndFlush(rating);
        return RatingVm.fromModel(savedRating);
    }

    public Double calculateAverageStar(Long productId){
        List<Object[]> totalStarsAndRatings = ratingRepository.getTotalStarsAndTotalRatings(productId);
        if(totalStarsAndRatings.get(0)[0] == null || totalStarsAndRatings.get(0)[1] == null)
            return 0.0;
        int totalStars = (Integer.parseInt(totalStarsAndRatings.get(0)[0].toString()));
        int totalRatings = (Integer.parseInt(totalStarsAndRatings.get(0)[1].toString()));

        Double averageStars = (totalStars * 1.0) / totalRatings;
        return averageStars;
    }
}
