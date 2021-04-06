package se.kth.ict.id2203.components.epfd;

import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

public class CheckTime extends Timeout
{
    public CheckTime(ScheduleTimeout request)
    {
        super(request);
    }
}