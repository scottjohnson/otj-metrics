/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opentable.metrics;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.management.MBeanServer;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import com.opentable.metrics.graphite.MetricSets;
import com.opentable.metrics.jvm.CpuLoadGauge;
import com.opentable.metrics.jvm.FileDescriptorMetricSet;
import com.opentable.metrics.jvm.GcMemoryMetrics;
import com.opentable.metrics.jvm.MemoryFreeMetricSet;
import com.opentable.metrics.jvm.NmtMetrics;

@Named
public class JvmMetricsConfiguration {
    private static final String BASE = "jvm";
    private final MetricRegistry metrics;
    private final MBeanServer mbs;
    private final NmtMetrics nmtMetrics;

    JvmMetricsConfiguration(final MetricRegistry metrics, final MBeanServer mbs) {
        this.metrics = metrics;
        this.mbs = mbs;
        nmtMetrics = new NmtMetrics(String.format("%s.nmt", BASE), metrics);
        new GcMemoryMetrics(String.format("%s.gc-mem", BASE), metrics);
    }

    private static MetricSet namespace(String namespace, MetricSet metrics) {
        return MetricSets.prefix(String.format("%s.%s.", BASE, namespace), metrics);
    }

    @PostConstruct
    void postConstruct() {
        metrics.registerAll(namespace("bufpool", new BufferPoolMetricSet(mbs)));
        metrics.registerAll(namespace("fd", new FileDescriptorMetricSet()));
        metrics.registerAll(namespace("gc", new GarbageCollectorMetricSet()));
        metrics.registerAll(namespace("mem", new MemoryUsageGaugeSet()));
        metrics.registerAll(namespace("mem", new MemoryFreeMetricSet()));
        metrics.registerAll(namespace("class", new ClassLoadingGaugeSet()));
        metrics.registerAll(namespace("thread", new ThreadStatesGaugeSet()));
        metrics.register(BASE + ".cpu.load", new CpuLoadGauge());

        nmtMetrics.register();
    }
}
