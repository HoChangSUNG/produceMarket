package creative.market;

import creative.market.service.GradeCriteriaService;
import creative.market.util.FileSubPath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static creative.market.util.FileSubPath.GRADE_CRITERIA_PATH;


@Component
@RequiredArgsConstructor
public class InitGradeCriteria {

    private final GradeCriteriaService gradeCriteriaService;

    public void saveGradeCriteria() {
        //식량 작물
        gradeCriteriaService.registerGradeCriteria("쌀", GRADE_CRITERIA_PATH +"쌀.png");
        gradeCriteriaService.registerGradeCriteria("찹쌀", GRADE_CRITERIA_PATH +"찹쌀.png");
        gradeCriteriaService.registerGradeCriteria("콩", GRADE_CRITERIA_PATH +"콩.png");
        gradeCriteriaService.registerGradeCriteria("팥", GRADE_CRITERIA_PATH +"팥.png");
        gradeCriteriaService.registerGradeCriteria("녹두", GRADE_CRITERIA_PATH +"녹두.png");
        gradeCriteriaService.registerGradeCriteria("메밀", GRADE_CRITERIA_PATH +"메밀.png");
        gradeCriteriaService.registerGradeCriteria("고구마", GRADE_CRITERIA_PATH +"고구마.png");
        gradeCriteriaService.registerGradeCriteria("감자", GRADE_CRITERIA_PATH +"감자.png");

        // 특용 작물
        gradeCriteriaService.registerGradeCriteria("참깨", GRADE_CRITERIA_PATH +"참깨.png");
        gradeCriteriaService.registerGradeCriteria("들깨", GRADE_CRITERIA_PATH +"들깨.png");
        gradeCriteriaService.registerGradeCriteria("땅콩", GRADE_CRITERIA_PATH +"땅콩.png");
        gradeCriteriaService.registerGradeCriteria("느타리버섯", GRADE_CRITERIA_PATH +"느타리버섯.png");
        gradeCriteriaService.registerGradeCriteria("팽이버섯", GRADE_CRITERIA_PATH +"팽이버섯.png");
        gradeCriteriaService.registerGradeCriteria("새송이버섯", GRADE_CRITERIA_PATH +"새송이버섯.png");

        //과일류
        gradeCriteriaService.registerGradeCriteria("사과", GRADE_CRITERIA_PATH +"사과.png");
        gradeCriteriaService.registerGradeCriteria("배", GRADE_CRITERIA_PATH +"배.png");
        gradeCriteriaService.registerGradeCriteria("복숭아", GRADE_CRITERIA_PATH +"복숭아.png");
        gradeCriteriaService.registerGradeCriteria("포도", GRADE_CRITERIA_PATH +"포도.png");
        gradeCriteriaService.registerGradeCriteria("감귤", GRADE_CRITERIA_PATH +"감귤.png");
        gradeCriteriaService.registerGradeCriteria("단감", GRADE_CRITERIA_PATH +"단감.png");
        gradeCriteriaService.registerGradeCriteria("바나나", GRADE_CRITERIA_PATH +"바나나.png");
        gradeCriteriaService.registerGradeCriteria("참다래", GRADE_CRITERIA_PATH +"참다래.png");
        gradeCriteriaService.registerGradeCriteria("오렌지", GRADE_CRITERIA_PATH +"오렌지.png");
        gradeCriteriaService.registerGradeCriteria("체리", GRADE_CRITERIA_PATH +"체리.png");
        gradeCriteriaService.registerGradeCriteria("레몬", GRADE_CRITERIA_PATH +"레몬.png");
        gradeCriteriaService.registerGradeCriteria("파인애플", GRADE_CRITERIA_PATH +"파인애플.png");
        gradeCriteriaService.registerGradeCriteria("망고", GRADE_CRITERIA_PATH +"망고.png");

        //채소류
        gradeCriteriaService.registerGradeCriteria("배추", GRADE_CRITERIA_PATH +"배추.png");
        gradeCriteriaService.registerGradeCriteria("양배추", GRADE_CRITERIA_PATH +"양배추.png");
        gradeCriteriaService.registerGradeCriteria("시금치", GRADE_CRITERIA_PATH +"시금치.png");
        gradeCriteriaService.registerGradeCriteria("상추", GRADE_CRITERIA_PATH +"상추.png");
        gradeCriteriaService.registerGradeCriteria("얼갈이배추", GRADE_CRITERIA_PATH +"얼갈이배추.png");
        gradeCriteriaService.registerGradeCriteria("수박", GRADE_CRITERIA_PATH +"수박.png");
        gradeCriteriaService.registerGradeCriteria("참외", GRADE_CRITERIA_PATH +"참외.png");
        gradeCriteriaService.registerGradeCriteria("오이", GRADE_CRITERIA_PATH +"오이.png");
        gradeCriteriaService.registerGradeCriteria("호박", GRADE_CRITERIA_PATH +"호박.png");
        gradeCriteriaService.registerGradeCriteria("토마토", GRADE_CRITERIA_PATH +"토마토.png");
        gradeCriteriaService.registerGradeCriteria("방울토마토", GRADE_CRITERIA_PATH +"방울토마토.png");
        gradeCriteriaService.registerGradeCriteria("딸기", GRADE_CRITERIA_PATH +"딸기.png");
        gradeCriteriaService.registerGradeCriteria("무", GRADE_CRITERIA_PATH +"무.png");
        gradeCriteriaService.registerGradeCriteria("당근", GRADE_CRITERIA_PATH +"당근.png");
        gradeCriteriaService.registerGradeCriteria("열무", GRADE_CRITERIA_PATH +"열무.png");
        gradeCriteriaService.registerGradeCriteria("건고추", GRADE_CRITERIA_PATH +"건고추.png");
        gradeCriteriaService.registerGradeCriteria("풋고추", GRADE_CRITERIA_PATH +"풋고추.png");
        gradeCriteriaService.registerGradeCriteria("꽈리고추", GRADE_CRITERIA_PATH +"꽈리고추.png");
        gradeCriteriaService.registerGradeCriteria("붉은고추", GRADE_CRITERIA_PATH +"붉은고추.png");
        gradeCriteriaService.registerGradeCriteria("청양고추", GRADE_CRITERIA_PATH +"청양고추.png");
        gradeCriteriaService.registerGradeCriteria("피마늘", GRADE_CRITERIA_PATH +"피마늘.png");
        gradeCriteriaService.registerGradeCriteria("깐마늘(국산)", GRADE_CRITERIA_PATH +"깐마늘.png");
        gradeCriteriaService.registerGradeCriteria("깐마늘(수입)", GRADE_CRITERIA_PATH +"깐마늘.png");
        gradeCriteriaService.registerGradeCriteria("양파", GRADE_CRITERIA_PATH +"양파.png");
        gradeCriteriaService.registerGradeCriteria("파", GRADE_CRITERIA_PATH +"파.png");
        gradeCriteriaService.registerGradeCriteria("생강", GRADE_CRITERIA_PATH +"생강.png");
        gradeCriteriaService.registerGradeCriteria("미나리", GRADE_CRITERIA_PATH +"미나리.png");
        gradeCriteriaService.registerGradeCriteria("깻잎", GRADE_CRITERIA_PATH +"깻잎.png");
        gradeCriteriaService.registerGradeCriteria("피망", GRADE_CRITERIA_PATH +"피망.png");
        gradeCriteriaService.registerGradeCriteria("파프리카", GRADE_CRITERIA_PATH +"파프리카.png");
        gradeCriteriaService.registerGradeCriteria("멜론", GRADE_CRITERIA_PATH +"멜론.png");


    }
}
