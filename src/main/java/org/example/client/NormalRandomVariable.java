package org.example.client;

import java.util.Random;

public class NormalRandomVariable {
    private double mean;
    private double stdDev;
    private Random random;
    
    public void setMean(double mean) {
        this.mean = mean;
    }

    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    public NormalRandomVariable(Random random, double mean, double stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
        this.random = random;
    }

    

    public NormalRandomVariable(double mean, double stdDev) {
        this.random = new Random(42);
        this.mean = mean;
        this.stdDev = stdDev;
    }

    public double nextValue() {
        return mean + stdDev * random.nextGaussian();
    }

    public static void main(String[] args) {
        double mean = 25;
        double stdDev = 10.0;
        NormalRandomVariable randomVariable = new NormalRandomVariable(new Random(), mean, stdDev);

        for (int i = 0; i < 20; i++) {
            System.out.println(randomVariable.nextValue());
        }
    }
}
