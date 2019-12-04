package tw.fondus.fews.adapter.pi.irrigation.nchc.util;

import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.CaseParameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.Parameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.type.FilterKeyWord;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The tools is used to control parameters.
 *
 * @author Brad Chen
 *
 */
public class ParameterUtils {
	private ParameterUtils(){}

	/**
	 * Collect the parameters to map with key: description, value: parameter.
	 *
	 * @param parameters
	 * @return
	 */
	public static Map<String, Parameter> toMap( List<Parameter> parameters ){
		return parameters.stream()
				.collect( Collectors.toMap(
						parameter -> parameter.getDescription(),
						parameter -> parameter
				) );
	}

	/**
	 * Filter the parameter by area key word.
	 *
	 * @param caseParameter
	 * @return
	 */
	public static List<Parameter> filterArea( CaseParameter caseParameter ){
		return filter( caseParameter, FilterKeyWord.AREA )
				.stream()
				.filter( parameter -> !parameter.getDescription().contains( FilterKeyWord.AREA_TOTAL.getValue() ) )
				.collect( Collectors.toList() );
	}

	/**
	 * Filter the parameter by draft key word.
	 *
	 * @param caseParameter
	 * @return
	 */
	public static List<Parameter> filterDraft( CaseParameter caseParameter ){
		return filter( caseParameter, FilterKeyWord.DRAFT );
	}

	/**
	 * Filter the parameter by plan water requirement key word.
	 *
	 * @param caseParameter
	 * @return
	 */
	public static List<Parameter> filterPlanWaterRequirement( CaseParameter caseParameter ){
		return filter( caseParameter, FilterKeyWord.PLAN_WATER_REQUIREMENT );
	}

	/**
	 * Filter the parameter by user-define key word.
	 *
	 * @param caseParameter
	 * @return
	 */
	public static List<Parameter> filterUserDefine( CaseParameter caseParameter ){
		return filter( caseParameter, FilterKeyWord.USER_DEFINE );
	}

	/**
	 * Filter the parameter by key word.
	 *
	 * @param caseParameter
	 * @param keyWord
	 * @return
	 */
	public static List<Parameter> filter( CaseParameter caseParameter, FilterKeyWord keyWord ){
		return caseParameter.getParameters().stream()
				.filter( parameter -> parameter.getDescription().contains( keyWord.getValue() ) )
				.sorted( Comparator.comparing( Parameter::getDescription ) )
				.collect( Collectors.toList() );
	}
}
