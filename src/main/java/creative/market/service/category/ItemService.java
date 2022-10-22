package creative.market.service.category;

import creative.market.repository.category.GradeCriteriaRepository;
import creative.market.repository.category.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final GradeCriteriaRepository gradeCriteriaRepository;

    @Transactional
    public void addGradeCriteriaId() {
        itemRepository.findAll().stream()
                .forEach(item -> item.addGradeCriteria(gradeCriteriaRepository.findByName(item.getName()).orElse(null)));
    }
}
