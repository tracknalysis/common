/**
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tracknalysis.common.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TimeUtilTest {
    
    @Test
    public void testFormatDuration() {
        
        assertEquals("-0:00.001", TimeUtil.formatDuration(-1l, false, true));
        assertEquals("-0:00.001", TimeUtil.formatDuration(-1l, true, true));
        assertEquals("-0:00", TimeUtil.formatDuration(-1l, true, false));
        
        assertEquals("0:00.001", TimeUtil.formatDuration(1l, false, true));
        assertEquals("0:00.001", TimeUtil.formatDuration(1l, true, true));
        assertEquals("0:00", TimeUtil.formatDuration(1l, true, false));
        
        assertEquals("0:01.001", TimeUtil.formatDuration(TimeUtil.MS_IN_SECOND + 1, false, true));
        assertEquals("0:01.001", TimeUtil.formatDuration(TimeUtil.MS_IN_SECOND + 1, true, true));
        assertEquals("0:01", TimeUtil.formatDuration(TimeUtil.MS_IN_SECOND + 1, false, false));
        
        assertEquals(
                "60:01.001",
                TimeUtil.formatDuration(TimeUtil.MS_IN_HOUR
                        + TimeUtil.MS_IN_SECOND + 1, false, true));
        assertEquals(
                "1:00:01.001",
                TimeUtil.formatDuration(TimeUtil.MS_IN_HOUR
                        + TimeUtil.MS_IN_SECOND + 1, true, true));
        assertEquals(
                "60:01",
                TimeUtil.formatDuration(TimeUtil.MS_IN_HOUR
                        + TimeUtil.MS_IN_SECOND + 1, false, false));
    }
}
