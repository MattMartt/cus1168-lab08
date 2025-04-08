package academy.javapro.lab08;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class InsuranceRatingEngine {
    // Knowledge base (facts about insurance rates)
    private Map<String, Object> knowledgeBase = new HashMap<>();
    // Rules list
    private List<Rule> rules = new ArrayList<>();
    // Constructor initializes the knowledge base and rules
    public InsuranceRatingEngine(){
        initializeKnowledgeBase();
        initializeRules();
    }

    private void initializeKnowledgeBase() {
        // Base rates by vehicle category
        knowledgeBase.put("baseRate.sedan", 1000.0);
        knowledgeBase.put("baseRate.suv", 1200.0);
        knowledgeBase.put("baseRate.luxury", 1500.0);
        knowledgeBase.put("baseRate.sports", 1800.0);

        // Age risk factors
        knowledgeBase.put("ageFactor.16-19", 2.0);
        knowledgeBase.put("ageFactor.20-24", 1.5);
        knowledgeBase.put("ageFactor.25-65", 1.0);
        knowledgeBase.put("ageFactor.66+", 1.3);

        // Accident surcharges
        knowledgeBase.put("accidentSurcharge.0", 0.0);
        knowledgeBase.put("accidentSurcharge.1", 300.0);
        knowledgeBase.put("accidentSurcharge.2+", 600.0);
    }

    private void initializeRules() {
        // Base rate rule - determines the starting premium based on vehicle type
        rules.add(new Rule("base rate",
            profile -> true, // Always applies
                (driver, premium) -> {
                    String vehicleCategory = determineVehicleCategory(driver);
                    double baseRate = (double) knowledgeBase.get("baseRate." + vehicleCategory);
                    premium.setBaseRate(baseRate);
                }
        ));
        Predicate<DriverProfile> profile = p -> true;
        BiConsumer<DriverProfile, Premium> action = (p, premium) -> {
            String vehicleCategory = determineVehicleCategory((DriverProfile) p);
            double baseRate = (double) knowledgeBase.get("baseRate." + vehicleCategory);
            premium.setBaseRate(baseRate);
        };
        
        // Age factor rule - adjusts premium based on driver's age
        rules.add(new Rule("age factor",
        driverProfile -> ((DriverProfile) driverProfile).getAge() > 0, // Check if age is valid
            (driverProfile, premium) -> {
                int age = ((DriverProfile) driverProfile).getAge();
                double factor = 1.0;
                String localExplanation = "";

                if (age < 20) {
                    factor = (double) knowledgeBase.get("ageFactor.16-19");
                    localExplanation = "Drivers under 20 have higher statistical risk";
                } else if (age < 25) {
                    factor = (double) knowledgeBase.get("ageFactor.20-24");
                    localExplanation = "Drivers 20-24 have moderately higher risk";
                } else if (age < 66) {
                    factor = (double) knowledgeBase.get("ageFactor.25-65");
                    localExplanation = "Standard rate for drivers 25-65";
                } else {
                    factor = (double) knowledgeBase.get("ageFactor.66+");
                    localExplanation = "Slight increase for senior drivers";
                }

                double adjustment = premium.getBaseRate() * (factor - 1.0);
                premium.addAdjustment("Age factor", adjustment, localExplanation);
            }
        ));
        BiConsumer<DriverProfile, Premium> ageAction = (p, premium) -> {
            int age = ((DriverProfile) p).getAge();
            double factor = 1.0;
            String localExplanation = "";

            if (age < 20) {
                factor = (double) knowledgeBase.get("ageFactor.16-19");
                localExplanation = "Drivers under 20 have higher statistical risk";
            } else if (age < 25) {
                factor = (double) knowledgeBase.get("ageFactor.20-24");
                localExplanation = "Drivers 20-24 have moderately higher risk";
            } else if (age < 66) {
                factor = (double) knowledgeBase.get("ageFactor.25-65");
                localExplanation = "Standard rate for drivers 25-65";
            } else {
                factor = (double) knowledgeBase.get("ageFactor.66+");
                localExplanation = "Slight increase for senior drivers";
            }

            double adjustment = premium.getBaseRate() * (factor - 1.0);
            premium.addAdjustment("Age factor", adjustment, localExplanation);
        };

        // Accident history rule - adds surcharges for recent accidents
        rules.add(new Rule("accident history",
            p -> ((DriverProfile) p).getAccidentsInLastFiveYears() > 0, // Check if there are any accidents in the last 5 years
            (driverProfile, premium) -> {
                int accidents = ((DriverProfile) driverProfile).getAccidentsInLastFiveYears(); // Use driverProfile instead of profile
                double surcharge = 0.0;
                String explanation = "";
        
                if (accidents == 1) {
                    surcharge = (double) knowledgeBase.get("accidentSurcharge.1");
                    explanation = "Surcharge for 1 accident in past 5 years";
                } else if (accidents > 1) {
                    surcharge = (double) knowledgeBase.get("accidentSurcharge.2+");
                    explanation = "Major surcharge for 2+ accidents in past 5 years";
                }
        
                premium.addAdjustment("Accident history", surcharge, explanation);
            }
        ));
        BiConsumer<DriverProfile, Premium> accidentAction = (p, premium) -> {
            int accidents = ((DriverProfile) p).getAccidentsInLastFiveYears();
            double surcharge = 0.0;
            String explanation = "";

            if (accidents == 1) {
                surcharge = (double) knowledgeBase.get("accidentSurcharge.1");
                explanation = "Surcharge for 1 accident in past 5 years";
            } else if (accidents > 1) {
                surcharge = (double) knowledgeBase.get("accidentSurcharge.2+");
                explanation = "Major surcharge for 2+ accidents in past 5 years";
            }

            premium.addAdjustment("Accident history", surcharge, explanation);
        };
    }

    // Helper method to determine vehicle category
    private String determineVehicleCategory(DriverProfile profile) {
        String make = profile.getVehicleMake();
        String model = profile.getVehicleModel();
        // Simple classification logic
        if (make.equalsIgnoreCase("bmw") || make.equalsIgnoreCase("mercedes") ||
            make.equalsIgnoreCase("lexus") || make.equalsIgnoreCase("audi")) {
            return "luxury";
        }
        if (make.equalsIgnoreCase("ferrari") || make.equalsIgnoreCase("porsche") ||
            model.equalsIgnoreCase("mustang") || make.equalsIgnoreCase("corvette")) {
            return "sports";
        }
        if (model.equalsIgnoreCase("suv") || model.equalsIgnoreCase("explorer") ||
            model.equalsIgnoreCase("tahoe") || model.equalsIgnoreCase("highlander")) {
            return "suv";
        }
        else{
            return "sedan";
        }
    }

    // Calculate premium by applying all applicable rules
    public Premium calculatePremium(DriverProfile profile) {
        Premium premium = new Premium();
        // Apply all rules that match the profile
        for (Rule rule : rules) {
            if (rule.matches(profile)) {
                rule.apply(profile, premium);
            }
        }
        return premium;
    }

    // Rule class
    static class Rule {
        private String name;
        private Predicate<DriverProfile> condition;
        private BiConsumer<DriverProfile, Premium> action;
        public Rule(String name, Predicate<DriverProfile> condition, BiConsumer<DriverProfile, Premium> action) {
            this.name = name;
            this.condition = condition;
            this.action = action;
        }

        public boolean matches(DriverProfile profile) {
            return condition.test(profile);
        }
        public void apply(DriverProfile profile, Premium premium) {
            action.accept(profile, premium);
        }
        public String getName() {
            return name;
        }
    }
}
