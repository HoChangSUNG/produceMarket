package creative.market.service;

import creative.market.repository.ItemCategoryRepository;
import creative.market.service.dto.ItemCategoryMenuDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemCategoryService {

    private final ItemCategoryRepository itemCategoryRepository;

    public List<ItemCategoryMenuDTO> findItemCategoryMenu() { //쿼리용 아닌 비즈니스용 메서드 추가되면 ItemCategoryQueryService 생성해서 추출하기
        return itemCategoryRepository.findAll().stream()
                .map(itemCategory -> new ItemCategoryMenuDTO(itemCategory))
                .collect(Collectors.toList());
    }
}
