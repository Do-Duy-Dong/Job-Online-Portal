package com.example.demo.cronjob;

import com.example.demo.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class cronJobExpire {
    @Autowired
    private JobRepository jobRepository;
    @Scheduled(cron = "0 0 0 * * *")
    public void expireJobs(){
        int batchSize= 100;
        int updatedCount=0;
        do{
            updatedCount = jobRepository.updateExpiredJobs(batchSize);
            try{
                if(updatedCount >0){
                    Thread.sleep(1000);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }while (updatedCount  >0);
    }
}
