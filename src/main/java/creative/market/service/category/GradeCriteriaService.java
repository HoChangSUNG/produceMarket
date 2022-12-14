package creative.market.service.category;

import creative.market.repository.category.GradeCriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeCriteriaService {

    private final GradeCriteriaRepository gradeCriteriaRepository;

    @Transactional
    public Long registerGradeCriteria(String name, String path) {
       return gradeCriteriaRepository.save(name, path);
    }
}
