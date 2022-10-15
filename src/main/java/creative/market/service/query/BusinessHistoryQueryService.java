package creative.market.service.query;

import creative.market.repository.BusinessHistoryRepository;
import creative.market.service.dto.BusinessHistoryRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessHistoryQueryService {

    private final BusinessHistoryRepository businessHistoryRepository;

    public List<BusinessHistoryRes> getBusinessHistoryList() {
        return businessHistoryRepository.findByStatusWithUserAndImage().stream()
                .map(businessHistory -> new BusinessHistoryRes(businessHistory))
                .collect(Collectors.toList());
    }
}
