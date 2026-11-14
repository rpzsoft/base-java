package com.rpzsoft.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


/**
 * simple retry mechanism ;)
 */
public class ExpBackOff {
    private  final Logger logger = LoggerFactory.getLogger(ExpBackOff.class);
    protected static final ConfigRegistry CONF = ConfigRegistry.getInstance();

    final float FACTOR=1.3f;
    int retryCnt =0;
    int maxRetryCnt=CONF.getIntProperty("transport.retry.max");
    int strategy=CONF.getIntProperty("transport.retry.strategy");
    long MAX=60*1000L; //1 min
    long MIN=500L;//0.5 sec
    long delay=MIN;
    Random rnd = null;

    public ExpBackOff() {
    }

    public ExpBackOff(long max, long min) {
        this.MAX = max;
        this.MIN = min;
    }

    public ExpBackOff maxRetryCnt(int maxRetryCnt) {
        this.maxRetryCnt = maxRetryCnt;
        return this;
    }

    public ExpBackOff strategy(int strategy) {
        this.strategy = strategy;
        return this;
    }

    public ExpBackOff max(long max) {
        this.MAX = max;
        return this;
    }

    public ExpBackOff min(long min) {
        this.MIN = min;
        return this;
    }

    public void reset(){
        retryCnt=0;
        delay=MIN;
    }

    public long nextDelay(){
        retryCnt++;
        if(delay < MAX) {
            if(strategy == 0) //simple exponential backoff
                delay *= FACTOR;
            else { //add jitter
                long pow =  (long)Math.pow(2, retryCnt) + rnd.nextInt(800) + MIN;
                if(pow < 0) delay=MAX;
                else delay = Math.min(MAX, pow);
            }
        }else delay = MAX;
        if(maxRetryCnt > 0 && retryCnt > maxRetryCnt){
            reset();
        }

        return delay;
    }

    public boolean shouldRetry(){
        boolean ret=true;

        try {
            delay = nextDelay();
            if(retryCnt == 0) ret = false;//we have reset the retry, to either give up, for try again with higher frequency
            else Thread.sleep(delay);
        } catch (InterruptedException e) {
            //
        } finally {
            if(ret)
                logger.info("retrying with delay {}ms ...", delay);
        }
        return ret;
    }

}
