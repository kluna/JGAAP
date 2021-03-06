package com.jgaap.eventCullers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jgaap.backend.Utils;
import com.jgaap.generics.Event;
import com.jgaap.generics.EventCuller;
import com.jgaap.generics.EventCullingException;
import com.jgaap.generics.EventHistogram;
import com.jgaap.generics.EventSet;
import com.jgaap.generics.Pair;

/**
 * Analyze N events with the lowest Coefficient of Variation
 * CoV = (stdev/mean)*100
 * 
 * @author Christine Gray
 */

public class CoefficientOfVariation extends EventCuller{
	public CoefficientOfVariation() {
		super();
		addParams("numEvents", "N", "50", new String[] { "1", "2", "3", "4",
				"5", "6", "7", "8", "9", "10", "15", "20", "25", "30", "40",
				"45", "50", "75", "100", "150", "200" }, true);
		addParams("Informative", "I", "Least", new String[] { "Most","Least"}, false);
	}
	@Override
	public List<EventSet> cull(List<EventSet> eventSets)
			throws EventCullingException {
		List<EventSet> results = new ArrayList<EventSet>();
		int minPos = Integer.parseInt(getParameter("minPos","0"));
		int numEvents = Integer.parseInt(getParameter("numEvents","50"));
		String informative = getParameter("Informative","Least");
		EventHistogram hist = new EventHistogram();

		for (EventSet oneSet : eventSets) {
			for (Event e : oneSet) {
				hist.add(e);
			}
		}
		
		List<Pair<Event,Double>> CoV = new ArrayList<Pair<Event,Double>>(); 
		List<EventHistogram> eventHistograms = new ArrayList<EventHistogram>(eventSets.size());
		for (EventSet eventSet : eventSets) {
			eventHistograms.add(new EventHistogram(eventSet));
		}
		
		for (Event event : hist) {
			double mean;
			double stddev;
			List<Double>frequencies = new  ArrayList<Double>();
			for (EventHistogram eventHistogram : eventHistograms) {
				frequencies.add((double) eventHistogram.getAbsoluteFrequency(event));
			}
			stddev = Utils.stddev(frequencies);
			mean = mean(frequencies,0.0,0.0);
			CoV.add(new Pair<Event,Double>(event, (stddev/mean)*100,2));
		}
		Collections.sort(CoV);
		if(informative.equals("Most")){
			Collections.reverse(CoV);
		}
		List<Event> Set = new ArrayList<Event>();
		for (int i = minPos; i < minPos + numEvents; i++) {
			Set.add(CoV.get(i).getFirst());
		}
		for (EventSet oneSet : eventSets) {
			EventSet newSet = new EventSet();
			for (Event e : oneSet) {
				if (Set.contains(e)) {
					newSet.addEvent(e);
				}
			}
			results.add(newSet);
		}		
		
		return results;
	}

	@Override
	public String displayName() {
		return "Coefficient of Variation";
	}

	@Override
	public String tooltipText() {
		return "Analyze N events with the lowest Coefficient of Variation";
	}

	@Override
	public boolean showInGUI() {
		return true;
	}
	@Override
	public String longDescription(){
		return "Analyze N events with the lowest Coefficient of Variation\n"+
				"CoV = (\u03C3/\u03BC)*100";
	}
	double mean(List<Double>frequencies, double count, double mean){
		if(frequencies.isEmpty()){
			return mean/count;
		}		
		mean+=frequencies.remove(0);
		count++;
		return mean(frequencies,count, mean);
	}
}
