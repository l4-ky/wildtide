package com.example.wildtide;
import com.example.wildtide.lockey.Manager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class WildtideApplication {
	public static void main(String[] args) {
		Manager.initStartup();
		SpringApplication.run(WildtideApplication.class, args);
	}
}

/*
 * open cmd from .jar target folder, then
 * scp * ciro@192.168.1.30:/home/ciro/Desktop/Wildtide/wildtide
 * 
 * documentation
 * https://www.raspberrypi.com/documentation/computers/remote-access.html#raspberry-pi-connect
 */

 //provare a ricavare la porta mittente dal controller Spring prima che entri nel mapping
 