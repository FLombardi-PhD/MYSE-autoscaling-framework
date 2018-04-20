package midlab.myse.ann;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Tools for MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class Tools {

	public static String incrementDay(String data){
		String nextYear = "";
		String nextMonth = "";
		String nextDay = "";
		String year;
		String month;
		String day;
		
		year = data.substring(0, 4);
		month = data.substring(5, 7);
		day = data.substring(8, 10);
		
		System.out.println("read date: "+year+"-"+month+"-"+day);
		
		if(month.equals("02")){
			if(day.equals("28")){
				nextDay = "01";
				nextMonth = "03";
				nextYear = year;
			}
			else{
				nextDay = increment(day);
				nextMonth = "02";
				nextYear = year;
			}
		}
		else if(month31(month)){
			if(day.equals("31")){
				if (month.equals("12")){
					nextYear = increment(year);
					nextDay = "01";
					nextMonth = "01";
				}
				else{
					nextYear = year;
					nextDay = "01";
					nextMonth = increment(month);
				}
			}
			else{
				nextYear = year;
				nextMonth = month;
				nextDay = increment(day);
			}
		}
		else{
			if(day.equals("30")){
					nextYear = year;
					nextDay = "01";
					nextMonth = increment(month);
			}
			else{
				nextYear = year;
				nextDay = increment(day);
				nextMonth = month;
			}
		}
		
		if(nextMonth.length()<2) nextMonth = "0"+nextMonth;
		if(nextDay.length()<2) nextDay = "0"+nextDay;
		return nextYear+"-"+nextMonth+"-"+nextDay;
	}
	
	private static boolean month31(String month){
		if(
			month.equals("04") ||
			month.equals("06") ||
			month.equals("09") ||
			month.equals("11")
			) return false;
		return true;
	}
	
	private static String increment(String gma){
		int num = Integer.parseInt(gma);
		num++;
		return ""+num;
	}
	
	public static void printResultTimeSlotHour(int[] traffic){
		for(int i=0; i<traffic.length; ++i){
			if(i==0) System.out.println("hour ; traffic");
			System.out.println(i+" ; "+traffic[i]);
		}
	}
	
	public static void printWeeklyResult(int[][] traffic){
		// LEGENDA: i=day; j=hour
		for(int j=0; j<traffic.length; ++j){
			for(int i=0; i<traffic[1].length; ++i){
				if(j==0 && i==0) System.out.println("hour ; traffic");
				System.out.println("day "+j+", hour "+i+" ; "+traffic[j][i]);
			}
		}
	}
	
	public static void printMonthlyResult(int[][] traffic){
		// LEGENDA: i=day; j=hour
		for(int j=0; j<traffic.length; ++j){
			for(int i=0; i<traffic[1].length; ++i){
				if(j==0 && i==0) System.out.println("hour\ttraffic");
				System.out.println(i+"\t"+traffic[j][i]);
			}
		}
	}
	
	public static String getYear(String date){
		return date.substring(0, 4);
	}
	
	public static String getMonth(String date){
		return date.substring(5, 7);
	}
	
	public static String getDay(String date){
		return date.substring(8, 10);
	}
	
	public static String getWeekDay(int day){
		return matchDay(day%7);
	}
	
	private static String matchDay(int day){
		String g = "domenica";       // sunday
		if(day==0) g = "domenica";   // sunday
		if(day==1) g = "lunedi";     // monday
		if(day==2) g = "martedi";    // tuesday
		if(day==3) g = "mercoledi";  // wednesday
		if(day==4) g = "giovedi";    // thursday
		if(day==5) g = "venerdi";    // friday
		if(day==6) g = "sabato";     // saturay
		
		return g;
	}
	
	public static int countRowsFile(String fileName) throws IOException{
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		int count = 0;
		while(reader.readLine()!=null) ++count;
		System.out.println("[Tools.countRowsFile] rows in " + fileName + ": " + count);
		return count;
	}
	
	public static double max(double[][] output){
		double max = 0.0;
		for(int i=0; i<output.length; ++i){
			if(output[i][0]>max) max = output[i][0];
		}
		return max;
	}
	
	public static double min(double[][] output){
		double min = -9999.0;
		for(int i=0; i<output.length; ++i){
			if(output[i][0]<min) min = output[i][0];
		}
		return min;
	}
	
	/** TODO: remove obsolete moethods **/
	
	/*
	public static double[][] normalizeOutput(int type, double[][] output){
		double max = max(output);
		double[][] norm = new double[output.length][1];
		//value:x=max:1 -> x=value:max
		for(int i=0; i<output.length; ++i){
			norm[i][0] = normalizeMaxMin01(type, output[i][0]);
		}
		return norm;
	}
	*/
	
	/*
	public static double normalizeMaxMin012(int type, double value){
		// Normalization [-1, 1]
		/** LEGENDA TYPES:
		 * Types = 0: input0 - weekDay (from [0, 6]   to [0, 1])
		 * Types = 1: input1 - day     (from [1, 31]  to [0, 1])
		 * Types = 2: input2 - month   (from [1, 12]  to [0, 1])
		 * Types = 3: input3 - hour    (from [0, 23]  to [0, 1])
		 * Types = 4: output - traffic (from [0, max] to [0, 1])
		 ********************************************************/
		/*
		double normalizedValue = 0.0;
		if(type==0){
			normalizedValue = ((value/6.0)*2.0)-1.0;
		}
		if(type==1){
			normalizedValue = ((value/23.0)*2.0)-1.0;
		}
		if(type==2){
			normalizedValue = ((value/ANN.max)*2.0)-1.0;
		}
		return normalizedValue;
	}
	*/
	
	/*
	public static double normalizzaMaxMin01(int type, double value){
		// Normalization [0, 1]
		/** LEGENDA TYPES:
		 * Types = 0: input0 - weekDay (from [0, 6]  to [0, 1])
		 * Types = 1: input1 - day     (from [1, 31] to [0, 1])
		 * Types = 2: input2 - month   (from [1, 12] to [0, 1])
		 * Types = 3: input3 - hour    (from [0, 23] to [0, 1])
		 * Types = 4: output ANN - traffic        (from [0, max]   to [0, 1])
		 * Types = 5: output ANN4 - traffic       (from [0, max]   to [0, 1])
		 * Types = 6: output ANNERR - error       (from [min, max] to [0, 1])
		 * Types = 7: input ANN_Traffic - traffic (from [0, max]   to [0, 1])
		 *********************************************************************/
		/*
		double normalizedValue = 0.0;
		if(type==0){
			normalizedValue = (value/6.0);
		}
		if(type==1){
			normalizedValue = (value/31.0);
		}
		if(type==2){
			normalizedValue = (value/12.0);
		}
		if(type==3){
			normalizedValue = (value/23.0);
		}
		if(type==4){
			normalizedValue = (value/ANN.max);
		}
		if(type==5){
			normalizedValue = (value/ANN4IN.max);
		}
		if(type==6){
			normalizedValue = ((value-ANNERR.min)/(ANNERR.max-ANNERR.min));
		}
		if(type==7){
			valoreNormalizzato = (value/ANN_Traffic.max);
		}
		*if(type==8){ // method moved in ANN_MLP
		*	normalizedValue = (value/ErrorChangingNumberIteration.max); 
		*}
		return normalizedValue;
	}*/
	
}
