package com.dcy.rpc.util;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Kyle
 * @date 2023/10/6 20:41
 * <p>
 * Generator for request id
 * <p>
 * The snowflake algorithm was the earliest unique ID generation algorithm used internally by Twitter in a distributed environment.
 * It uses 64-bit long type data storage. Details as follows:
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 0000000000 - 000000000000
 * Sign bit - Timestamp - Machine code - Serial number
 * The highest bit represents the sign bit, where 0 represents an integer, 1 represents a negative number, and ids are generally positive numbers, so the highest bit is 0.
 * <p>
 * Implemented through the snowflake algorithm -- (no snowflake in the world is the same) 5+5+42+12=64 bits
 *  - Computer room number (data center) 5 bits
 *  - Machine number 5bit
 *  - The time originally represented by 64 bits of the timestamp (long) must be reduced to 42 bits
 *  - Serialization 12bit: The same machine number in the same computer room at the same time may require many IDs due to concurrency.
 */
public class IdGenerator {

    // start timestamp
    private static final long START_STAMP = DateUtil.get("2024-1-1").getTime();

    // Computer room number
    public static final long DATA_CENTER_BIT = 5L;

    // machine code
    public static final long MACHINE_BIT = 5L;

    // Serialization number
    public static final long SEQUENCE_BIT = 5L;

    // The maximum value of the computer room number
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);

    // The maximum value of the machine number
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);

    // Maximum value of serial number
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    // The number of digits that the timestamp needs to be shifted to the left
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;

    // The number of digits that the computer room number needs to be shifted to the left
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;

    // The number of digits that the machine number needs to be shifted to the left
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    // Data center number
    private final long dataCenterId;

    // machinery code
    private final long machineId;

    // Sequence id
    private final LongAdder sequenceId = new LongAdder();

    private long lastTimeStamp = -1;

    public IdGenerator(long dataCenterId, long machineId) {
        // Check whether the parameters are legal
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("The data center number and machine number passed in are illegal");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        // 1.Dealing with timestamp issues
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_STAMP;

        // 2.Determine clock callback
        if (timeStamp < lastTimeStamp) {
            throw new RuntimeException("The server performed a clock callback");
        }

        // 3.Do some processing on sequenceId: if it is the same time node, it must be incremented
        if (timeStamp == lastTimeStamp) {
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX) {
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }
        // 4.At the end of execution, assign the timestamp to lastTimeStamp
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT | machineId << MACHINE_LEFT | sequence;
    }

    private long getNextTimeStamp() {
        // Get the current timestamp
        long current = System.currentTimeMillis() - START_STAMP;
        // If the same, keep looping until the next timestamp
        while (current == lastTimeStamp) {
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }
}
