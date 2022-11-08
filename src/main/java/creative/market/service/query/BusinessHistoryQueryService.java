package creative.market.service.query;

import creative.market.domain.business.BusinessHistory;
import creative.market.repository.BusinessHistoryRepository;
import creative.market.service.dto.BusinessHistoryRes;
import creative.market.service.dto.BusinessHistoryUserRes;
import creative.market.service.dto.UserBusinessHistoryRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessHistoryQueryService {

    private final BusinessHistoryRepository businessHistoryRepository;

    public BusinessHistoryRes getBusinessHistory(Long businessId) {
        BusinessHistory businessHistory = businessHistoryRepository.findByIdWithUserAndImage(businessId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 신청입니다."));

        return new BusinessHistoryRes(businessHistory);
    }

    public List<BusinessHistoryUserRes> getBusinessHistoryUserList() {
        return businessHistoryRepository.findByStatusWithUser().stream()
                .map(BusinessHistoryUserRes::new)
                .collect(Collectors.toList());
    }

    public List<UserBusinessHistoryRes> getUserBusinessHistoryList(Long userId) {
        return businessHistoryRepository.findByUser(userId).stream()
                .map(UserBusinessHistoryRes::new)
                .collect(Collectors.toList());
    }
}
