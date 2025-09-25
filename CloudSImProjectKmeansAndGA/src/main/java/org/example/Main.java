package org.example;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    static final int NUM_HOSTS = 30;
    static final int NUM_VMS = 10;
    static final int K_CLUSTERS = 5;
    static final int GA_POPULATION = 20;
    static final int GA_GENERATIONS = 20;

    public static void main(String[] args) {
        try {
            CloudSim.init(1, Calendar.getInstance(), false);

            Datacenter datacenter = createDatacenter("Datacenter");
            DatacenterBroker broker = new DatacenterBroker("Broker");

            List<Vm> vms = createVMs(broker.getId(), NUM_VMS);
            List<Cloudlet> cloudlets = createCloudlets(broker.getId(), NUM_VMS);

            Map<Integer, List<Vm>> clusters = kMeansClustering(vms, K_CLUSTERS);
            System.out.println("\n=== VM Clusters (K-Means) ===");
            for (Map.Entry<Integer, List<Vm>> entry : clusters.entrySet()) {
                System.out.println("Cluster " + entry.getKey() + ": "
                        + entry.getValue().stream()
                        .map(vm -> String.valueOf(vm.getId()))
                        .collect(Collectors.joining(", ")));
            }

            int[] bestAllocation = runGA(datacenter.getHostList(), vms, GA_POPULATION, GA_GENERATIONS);

            // 👇 1. Manual VM allocation based on GA
            allocateVMsByAllocation(datacenter, vms, bestAllocation);

            // 👇 2. Register VMs with broker so it can assign cloudlets to them
            broker.submitVmList(vms);

            // 👇 3. Submit cloudlets
            broker.submitCloudletList(cloudlets);

            // ADDED: Start measuring wall clock time before simulation
            long startTime = System.currentTimeMillis();  // ADDED

            // 👇 4. Start simulation
            CloudSim.startSimulation();

            // ADDED: End measuring wall clock time after simulation
            long endTime = System.currentTimeMillis();  // ADDED
            long wallClockMillis = endTime - startTime; // ADDED

            List<Cloudlet> results = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(results);

            // Count successful cloudlets
            long successfulCloudlets = results.stream()
                    .filter(c -> c.getStatus() == Cloudlet.SUCCESS)
                    .count();

            // Get last cloudlet finish time in seconds
            double lastFinishTime = results.stream()
                    .mapToDouble(Cloudlet::getFinishTime)
                    .max()
                    .orElse(0.0);

            // Print summary
            System.out.println("\n=== Simulation Summary ===");
            System.out.printf("Total successful cloudlets: %d\n", successfulCloudlets);

            // Print energy and cost summary
            printEnergySummary(datacenter.getHostList(), lastFinishTime);

            // ADDED: Print detailed additional metrics after simulation
            printDetailedMetrics(results, vms, datacenter.getHostList(), lastFinishTime, wallClockMillis);  // ADDED

        } catch (Exception e) {
            e.printStackTrace(); // or log it
        }
    }

    // Put this method here inside the class
    public static double calculateAverageHostUtilization(List<Host> hostList) {
        double totalUtilization = 0.0;
        int hostCount = hostList.size();

        for (Host host : hostList) {
            double hostUtil = 0.0;

            for (Pe pe : host.getPeList()) {
                hostUtil += pe.getPeProvisioner().getUtilization();
            }

            hostUtil /= host.getPeList().size();

            totalUtilization += hostUtil;
        }

        return (hostCount > 0) ? totalUtilization / hostCount : 0.0;
    }

    // Modified printEnergySummary with cost calculations added
    public static void printEnergySummary(List<Host> hosts, double simulationSeconds) {
        double simulationHours = simulationSeconds / 3600.0;

        double powerIdle = 0.1; // kW per host
        double powerMax = 0.4;  // kW per host

        double averageUtilization = calculateAverageHostUtilization(hosts);

        double actualPowerPerHost = powerIdle + averageUtilization * (powerMax - powerIdle);

        double baselineEnergy = hosts.size() * powerMax * simulationHours;
        double actualEnergy = hosts.size() * actualPowerPerHost * simulationHours;
        double energySaved = baselineEnergy - actualEnergy;

        double efficiency = (energySaved / baselineEnergy) * 100.0;

        // Cost calculations
        double costPerKWh = 0.12; // USD per kWh (example)
        double baselineCost = baselineEnergy * costPerKWh;
        double actualCost = actualEnergy * costPerKWh;
        double costSaved = baselineCost - actualCost;

        System.out.println("\n=== Energy and Cost Summary ===");
        System.out.printf("Baseline energy (all hosts max power) (kWh): %.6f\n", baselineEnergy);
        System.out.printf("Actual energy consumed (kWh): %.6f\n", actualEnergy);
        System.out.printf("Total energy saved (kWh): %.6f\n", energySaved);
        System.out.printf("Simulation time (hours): %.2f\n", simulationHours);
        System.out.printf("Efficiency (%%): %.2f%%\n", efficiency);

        System.out.printf("Baseline cost (USD): $%.4f\n", baselineCost);
        System.out.printf("Actual cost (USD): $%.4f\n", actualCost);
        System.out.printf("Total cost saved (USD): $%.4f\n", costSaved);
    }

    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < NUM_HOSTS; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                peList.add(new Pe(j, new PeProvisionerSimple(2000)));
            }
            Host host = new Host(i, new RamProvisionerSimple(8192), new BwProvisionerSimple(10000), 1000000, peList, new VmSchedulerTimeShared(peList));
            hostList.add(host);
        }
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics("x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.001, 0.0);
        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
    }

    private static List<Vm> createVMs(int brokerId, int count) {
        Random rand = new Random();
        List<Vm> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int mips = 250 + rand.nextInt(200); // Random MIPS between 250 and 450
            Vm vm = new Vm(i, brokerId, mips, 1, 512, 1000, 10000, "Xen", new CloudletSchedulerTimeShared());
            list.add(vm);
        }
        return list;
    }

    private static List<Cloudlet> createCloudlets(int brokerId, int count) {
        Random rand = new Random();
        List<Cloudlet> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long length = 50000 + rand.nextInt(20000); // Random length between 30000 and 50000
            Cloudlet cloudlet = new Cloudlet(i, length, 1, 300, 300,
                    new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            cloudlet.setUserId(brokerId);
            list.add(cloudlet);
        }
        return list;
    }

    private static Map<Integer, List<Vm>> kMeansClustering(List<Vm> vmList, int k) {
        List<Double> mipsList = vmList.stream().map(vm -> (double) vm.getMips()).collect(Collectors.toList());
        double[] centroids = new double[k];
        for (int i = 0; i < k; i++) {
            centroids[i] = mipsList.get(i);
        }

        Map<Integer, List<Vm>> clusters;
        boolean changed;
        do {
            clusters = new HashMap<>();
            for (int i = 0; i < k; i++) {
                clusters.put(i, new ArrayList<>());
            }
            for (Vm vm : vmList) {
                double minDist = Double.MAX_VALUE;
                int cluster = 0;
                for (int i = 0; i < k; i++) {
                    double dist = Math.abs(vm.getMips() - centroids[i]);
                    if (dist < minDist) {
                        minDist = dist;
                        cluster = i;
                    }
                }
                clusters.get(cluster).add(vm);
            }
            changed = false;
            for (int i = 0; i < k; i++) {
                double newCentroid = clusters.get(i).stream().mapToDouble(Vm::getMips).average().orElse(centroids[i]);
                if (newCentroid != centroids[i]) {
                    centroids[i] = newCentroid;
                    changed = true;
                }
            }
        } while (changed);
        return clusters;
    }

    private static int[] runGA(List<Host> hosts, List<Vm> vms, int populationSize, int generations) {
        Random rand = new Random();
        int numVMs = vms.size();
        int numHosts = hosts.size();

        int[][] population = new int[populationSize][numVMs];
        for (int i = 0; i < populationSize; i++) {
            List<Integer> hostIds = new ArrayList<>();
            for (int h = 0; h < numHosts; h++) {
                hostIds.add(h);
            }
            Collections.shuffle(hostIds);  // randomize host order
            for (int j = 0; j < numVMs; j++) {
                population[i][j] = hostIds.get(rand.nextInt(numHosts));  // assign random host from full range
            }
        }

        int[] bestIndividual = population[0];
        double bestFitness = Double.MAX_VALUE;

        for (int gen = 0; gen < generations; gen++) {
            for (int i = 0; i < populationSize; i++) {
                double fitness = evaluateFitness(population[i], hosts, vms);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestIndividual = population[i].clone();
                }
            }
            for (int i = 1; i < populationSize; i++) {
                int[] parent1 = bestIndividual;
                int[] parent2 = population[rand.nextInt(populationSize)];
                int[] child = new int[numVMs];
                for (int j = 0; j < numVMs; j++) {
                    child[j] = rand.nextDouble() < 0.5 ? parent1[j] : parent2[j];
                }
                if (rand.nextDouble() < 0.2) // mutation
                {
                    child[rand.nextInt(numVMs)] = rand.nextInt(numHosts);
                }
                population[i] = child;
            }
        }

        System.out.println("\n=== GA Best Allocation ===");
        System.out.println("VMID\tMIPS\tAssigned Host");
        for (int i = 0; i < bestIndividual.length; i++) {
            System.out.println(i + "\t" + vms.get(i).getMips() + "\t" + bestIndividual[i]);
        }
        return bestIndividual;
    }

    private static double evaluateFitness(int[] allocation, List<Host> hosts, List<Vm> vms) {
        int[] cpuLoad = new int[hosts.size()];
        for (int i = 0; i < allocation.length; i++) {
            cpuLoad[allocation[i]] += vms.get(i).getMips();
        }
        double imbalance = 0.0;
        double avgLoad = Arrays.stream(cpuLoad).average().orElse(1);
        for (int load : cpuLoad) {
            imbalance += Math.abs(load - avgLoad);
        }
        return imbalance;
    }

    private static void allocateVMsByAllocation(Datacenter datacenter, List<Vm> vms, int[] allocation) {
        List<Host> hosts = datacenter.getHostList();
        for (int i = 0; i < vms.size(); i++) {
            Host host = hosts.get(allocation[i]);
            if (host.vmCreate(vms.get(i))) {
                System.out.printf("VM #%d allocated to Host #%d\n", vms.get(i).getId(), host.getId());
            } else {
                System.out.printf("Failed to allocate VM #%d to Host #%d\n", vms.get(i).getId(), host.getId());
            }
        }
    }

    private static void printCloudletList(List<Cloudlet> cloudletList) {
        System.out.println("\n=== Cloudlet Results ===");
        System.out.println("CloudletID\tSTATUS\tVMID\tTime\tStart\tFinish");
        for (Cloudlet c : cloudletList) {
            if (c.getStatus() == Cloudlet.SUCCESS) {
                System.out.printf("%d\t	SUCCESS\t%d\t%.2f\t%.2f\t%.2f\n",
                        c.getCloudletId(), c.getVmId(), c.getActualCPUTime(), c.getExecStartTime(), c.getFinishTime());
            }
        }
    }

    // Put this method here inside the class (below your existing methods)
    public static void printDetailedMetrics(
            List<Cloudlet> cloudlets,
            List<Vm> vms,
            List<Host> hosts,
            double simulationSeconds,
            long wallClockMillis) {

        int totalCloudlets = cloudlets.size();
        int successful = (int) cloudlets.stream().filter(c -> c.getStatus() == Cloudlet.SUCCESS).count();
        int failed = totalCloudlets - successful;

        double avgExecTime = cloudlets.stream()
                .filter(c -> c.getStatus() == Cloudlet.SUCCESS)
                .mapToDouble(c -> c.getFinishTime() - c.getExecStartTime())
                .average()
                .orElse(0);

        double throughput = (simulationSeconds > 0) ? (successful / simulationSeconds) : 0;

        List<Double> hostLoads = hosts.stream()
                .map(host -> {
                    double util = 0;
                    for (Pe pe : host.getPeList()) {
                        util += pe.getPeProvisioner().getUtilization();
                    }
                    return util / host.getPeList().size();
                })
                .collect(Collectors.toList());

        double meanLoad = hostLoads.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = hostLoads.stream().mapToDouble(l -> (l - meanLoad) * (l - meanLoad)).average().orElse(0);
        double stdDevLoad = Math.sqrt(variance);

        long overloadedCount = hostLoads.stream().filter(l -> l > 0.9).count();

        double simulationHours = simulationSeconds / 3600.0;
        double powerIdle = 0.1;
        double powerMax = 0.4;
        double avgUtil = calculateAverageHostUtilization(hosts);
        double actualPowerPerHost = powerIdle + avgUtil * (powerMax - powerIdle);
        double actualEnergy = hosts.size() * actualPowerPerHost * simulationHours;

        double energyPerCloudlet = (successful > 0) ? actualEnergy / successful : 0;
        double energyPerVm = (vms.size() > 0) ? actualEnergy / vms.size() : 0;

        double costPerKWh = 0.12;
        double costPerHost = actualPowerPerHost * simulationHours * costPerKWh;
        double costPerVm = (vms.size() > 0) ? (actualEnergy * costPerKWh) / vms.size() : 0;

        System.out.println("\n=== Detailed Simulation Metrics ===");
        System.out.printf("Average Cloudlet Execution Time (s): %.3f\n", avgExecTime);
        System.out.printf("Cloudlet Throughput (cloudlets/sec): %.5f\n", throughput);
        System.out.printf("Cloudlet Failure Count: %d\n", failed);
        System.out.printf("CPU Load Standard Deviation: %.4f\n", stdDevLoad);
        System.out.printf("Number of Overloaded Hosts (>90%% utilization): %d\n", overloadedCount);
        System.out.printf("Energy consumed per successful Cloudlet (kWh): %.6f\n", energyPerCloudlet);
        System.out.printf("Energy consumed per VM (kWh): %.6f\n", energyPerVm);
        System.out.printf("Cost per Host (USD): $%.4f\n", costPerHost);
        System.out.printf("Cost per VM (USD): $%.4f\n", costPerVm);
        System.out.printf("Simulation Runtime (Wall Clock, ms): %d\n", wallClockMillis);
    }

}
