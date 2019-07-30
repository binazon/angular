package com.orange.srs.refreport.business;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

public class ParamLogicalConnectComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		int o1Weight = getWeight(o1);
		int o2Weight = getWeight(o2);
		if (o1Weight != o2Weight) {
			return o1Weight - o2Weight;
		} else {
			return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
		}
	}

	private int getWeight(String str) {
		int weight = 0;
		if (str.equals("*")) {
			weight = 4;
		} else {
			switch (StringUtils.countMatches(str, "*")) {
			case 0:
				weight = 0;
				break;
			case 1:
				if (str.endsWith("*")) {
					weight = 1;
				} else if (str.startsWith("*")) {
					weight = 2;
				} else {
					weight = 5;
				}
				break;
			case 2:
				if (str.startsWith("*") && str.endsWith("*")) {
					weight = 3;
				} else {
					weight = 5;
				}
				break;
			default:
				weight = 5;
			}
		}
		return weight;
	}

}
