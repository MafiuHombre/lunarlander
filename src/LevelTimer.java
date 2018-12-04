import java.awt.event.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.Timer;

public class LevelTimer {
	public LevelTimer() {
		counter = 0;
		date = new Date(1,1,10,0,0);
		TimeCounter timeCounter = new TimeCounter();
		timer = new Timer(1000, timeCounter);
		timer.start();
	}
	
	
	
	
	
	/*
	 * Klasa wewnetrzna zwiekszajaca wartosc licznika co sekunde o 1
	 */


	class TimeCounter implements ActionListener {
		public void actionPerformed(ActionEvent event)
		{
			counter++;
			if(counter == 60) {
				date.setMinutes(date.getMinutes() + 1);
				counter = 0;
				date.setSeconds(counter);
			}
			else
				date.setSeconds(counter);
//			System.out.println(date.getMinutes() + ":" + date.getSeconds());
		}
		
	}
	public Date getDate() {return date;}
	
	private Timer timer;
	private int counter;
	private Date date;
}