package creative.market.service;

import creative.market.repository.GradeCriteriaRepository;
import creative.market.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
