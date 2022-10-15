package creative.market.service;

import creative.market.domain.business.BusinessHistory;
import creative.market.domain.business.BusinessImage;
import creative.market.domain.business.BusinessStatus;
import creative.market.exception.DuplicateException;
import creative.market.repository.BusinessHistoryRepository;
import creative.market.repository.user.BuyerRepository;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileSubPath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessHistoryService {

    private final BusinessHistoryRepository businessHistoryRepository;
    private final BuyerRepository buyerRepository;

    @Transactional
    public Long createBusiness(BusinessHistory businessHistory) {

        businessHistoryRepository.findByUserIdAndStatus(businessHistory.getUser().getId())
                .ifPresent((b) -> {
                    throw new DuplicateException("이미 사업자를 신청했습니다");
                });

        return businessHistoryRepository.save(businessHistory);
    }

    @Transactional
    public void acceptBusiness(Long businessId) {
        BusinessHistory findBusiness = businessHistoryRepository.findById(businessId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 신청입니다"));

        findBusiness.changeStatus(BusinessStatus.ACCEPT);

        buyerRepository.updateType(findBusiness.getUser().getId());
    }

    @Transactional
    public void rejectBusiness(Long businessId) {
        BusinessHistory findBusiness = businessHistoryRepository.findById(businessId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 신청입니다"));

        findBusiness.changeStatus(BusinessStatus.REJECT);
    }
}
