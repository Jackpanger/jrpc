package com.jackpang;

import java.util.Date;
import java.util.concurrent.atomic.LongAdder;

/**
 * description: IdGenerator
 * date: 11/5/23 3:59 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class IdGenerator {
    private static LongAdder longAdder = new LongAdder();

//    /**
//     * stand-alone version
//     * @return id
//     */
//    public static long getId() {
//        longAdder.increment();
//        return longAdder.sum();
//    }

    // Snowflake Algorithm
    // cluster number 5bit 32
    // machine number 5bit 32
    // timestamp(long 1970-1-1)
    // from the date of the birth of the company
    // sequence number 12bit 5+5+42+12 = 64

    // start timestamp
    public static final long START_TIMESTAMP = DateUtils.get("2022-01-01").getTime();

    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;
    // max value of data center
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    // max value of machine
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    // max value of sequence
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    // timestamp(42) + data center(5) + machine(5) + sequence(12)
    public static final long TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT + DATA_CENTER_BIT;
    public static final long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;
    private Long dataCenterId;
    private Long machineId;
    private LongAdder sequenceId = new LongAdder();
    // Clock Backtracking issue
    private Long lastTimestamp = -1L;

    public IdGenerator(Long dataCenterId, Long machineId) {
        // check if the data center id and machine id are valid
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("data center id can't be greater than " + DATA_CENTER_MAX + " or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        // 1. timestamp issue
        long currentTimestamp = System.currentTimeMillis();
        long timeStamp = currentTimestamp - START_TIMESTAMP;
        // check clock backtracking
        if (timeStamp < lastTimestamp) {
            throw new RuntimeException("clock is backtracking, please check your system time");
        }

        // sequence issue
        if (timeStamp == lastTimestamp){
            if (sequenceId.sum() == SEQUENCE_MAX){
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
            sequenceId.increment();
        }else {
            sequenceId.reset();
        }
        lastTimestamp = timeStamp;
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT | machineId << MACHINE_LEFT | sequenceId.sum();
    }

    private long getNextTimeStamp() {
        long currentTimestamp = System.currentTimeMillis()-START_TIMESTAMP;
        while (currentTimestamp == lastTimestamp){
            currentTimestamp = System.currentTimeMillis()-START_TIMESTAMP;
        }
        return currentTimestamp;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1L, 1L);

        for (int i = 0; i < 10000; i++) {
            new Thread(() -> {
                System.out.println(idGenerator.getId());
            }).start();
        }
    }
}
