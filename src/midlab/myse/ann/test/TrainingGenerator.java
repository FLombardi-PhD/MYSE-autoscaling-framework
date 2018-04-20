package midlab.myse.ann.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import name.pachler.nio.file.ClosedWatchServiceException;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;
import name.pachler.nio.file.ext.Bootstrapper;

/**
 * Training generator class of MYSE.
 * It reads traffic values realtime to generate the training set
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class TrainingGenerator {
	
	public static void main(String[] args){
		new TrainingGenerator().test();
	}
	
	public void test(){

		@SuppressWarnings("unused") // TODO: check whether to remove it
		long mill = Bootstrapper.getDefaultPollingInterval();
		
		Bootstrapper.setDefaultPollingInterval(10000);
		boolean bool = Bootstrapper.isForcePollingEnabled();
		if(!bool) Bootstrapper.setForcePollingEnabled(true);
		mill = Bootstrapper.getDefaultPollingInterval();
		
		WatchService watchService = Bootstrapper.newWatchService();
		Path watchedPath = Bootstrapper.newPath(new File("C:/Users/myse/MYSE/test"));
		
		@SuppressWarnings("unused")
		WatchKey key = null;
		try{
			key = watchedPath.register(watchService, StandardWatchEventKind.ENTRY_MODIFY);
		} catch(UnsupportedOperationException uox){
			System.err.println("Evento di watching non supportato");
		} catch(IOException iox){
			System.err.println("IO errors");
		}
		
		for( ; ; ){
			WatchKey signalledKey = null;
			try{
				// take() stop the thread as long as a file is modified
				signalledKey = watchService.take();
			} catch(InterruptedException ix){
				// ignore the exception if any other thread invoked the interrupt method on the watch service
				continue;
			} catch(ClosedWatchServiceException cwse){
				System.out.println("watch service closed, terminating.");
				break;
			}
			
			// get the list of observed events from object key
			List<WatchEvent<?>> list = signalledKey.pollEvents();
			
			// reset the list of events from object key
			signalledKey.reset();
			
			// print what's happen in the monitored directory
			for(@SuppressWarnings("rawtypes") WatchEvent e : list){
				String message = "";
				if(e.kind() == StandardWatchEventKind.ENTRY_MODIFY){
					Path context = (Path)e.context();
					message = context.toString()+" modified";
					
					/*
					 *  TODO: here we have to invoke the learning of myse on new training set
					 */
					
				}
				else if(e.kind() == StandardWatchEventKind.OVERFLOW){
					message = "OVERFLOW: too many events than it is possible to monitor.";
				}
				System.out.println(message);
			}
		}
	}	
	
}
