package com.thesnellai.luckydna.services;

import com.thesnellai.luckydna.arenas.*;
import com.thesnellai.luckydna.dto.PredictionRequest;
import com.thesnellai.luckydna.dto.PredictionResponse;
import com.thesnellai.luckydna.framework.PredictionMatrixResult;
import com.thesnellai.luckydna.models.PredictionModel;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;

@Service
public class PredictionMatrixService {

    private static final int PRIMARY_SIGNAL_MIN = 1;
    private static final int PRIMARY_SIGNAL_MAX = 100;
    private static final int BONUS_SIGNAL_MIN = 1;
    private static final int BONUS_SIGNAL_MAX = 100;

    private static final int DEFAULT_PRIMARY_CANDIDATE_COUNT = 5;
    private static final int MONTE_CARLO_ITERATIONS = 10_000;

    private final ArenaRegistry arenaRegistry;

    public PredictionMatrixService(ArenaRegistry arenaRegistry) {
        this.arenaRegistry = arenaRegistry;
    }

    public PredictionResponse runPrediction(
            Long userId,
            String email,
            PredictionRequest request
    ) {
        int gameCount = request == null || request.gameCount() == null
                ? 1
                : Math.max(1, Math.min(request.gameCount(), 10));

        boolean autoSavePredictions =
                request != null && Boolean.TRUE.equals(request.autoSavePredictions());

        LocalDate effectiveDrawDate = request == null || request.drawDate() == null
                ? LocalDate.now()
                : request.drawDate();

        String predictionRunId =
                request == null || request.predictionRunId() == null || request.predictionRunId().isBlank()
                        ? UUID.randomUUID().toString()
                        : request.predictionRunId();

        ArenaCode arenaCode = request == null || request.arenaCode() == null
                ? ArenaCode.POWERBALL
                : request.arenaCode();

        Set<PredictionModel> activePredictionModels =
                request == null || request.enabledModels() == null || request.enabledModels().isEmpty()
                        ? Set.of(
                        PredictionModel.DNA_PICK,
                        PredictionModel.STAT_PICK,
                        PredictionModel.BALANCED_PICK,
                        PredictionModel.MONTE_CARLO_PICK,
                        PredictionModel.BAYESIAN_PICK)
                        : request.enabledModels();

        ArenaAdapter arenaAdapter = arenaRegistry.getAdapter(arenaCode);

        List<ArenaPrediction> arenaPredictions = new ArrayList<>();
        List<String> explanations = new ArrayList<>();

        for (int gameNumber = 1; gameNumber <= gameCount; gameNumber++) {
            Set<PredictionModel> gameModels = modelsForGame(
                    activePredictionModels,
                    gameNumber);

            PredictionMatrixResult gameMatrixResult = buildGameMatrix(
                    userId,
                    email,
                    effectiveDrawDate,
                    predictionRunId,
                    gameNumber,
                    gameModels,
                    arenaAdapter.specification().displayName());

            arenaPredictions.add(arenaAdapter.apply(gameMatrixResult));

            explanations.add(
                    "Game " + gameNumber
                            + " used "
                            + String.join(", ", gameMatrixResult.methodsUsed())
                            + ".");
        }

        if (autoSavePredictions) {
            explanations.add("Auto-save was requested. Predictions should be stored as non-purchased prediction records.");
        }

        double responseMatrixScore = arenaPredictions.stream()
                .mapToDouble(ArenaPrediction::predictionScore)
                .average()
                .orElse(0);

        return new PredictionResponse(
                arenaPredictions,
                responseMatrixScore,
                autoSavePredictions,
                explanations
        );
    }

    private Map<Integer, List<String>> initializeReasonMap(
            int minValue,
            int maxValue
    ) {
        Map<Integer, List<String>> reasons = new HashMap<>();

        for (int value = minValue; value <= maxValue; value++) {
            reasons.put(value, new ArrayList<>());
        }

        return reasons;
    }

    private Map<Integer, List<String>> copyReasonMap(
            Map<Integer, List<String>> original
    ) {
        Map<Integer, List<String>> copy = new HashMap<>();

        for (var entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return copy;
    }

    private void addReason(
            Map<Integer, List<String>> reasons,
            Integer number,
            String reason
    ) {
        reasons.computeIfAbsent(number, key -> new ArrayList<>()).add(reason);
    }

    private void addShiftReasons(
            Map<Integer, Double> scores,
            Map<Integer, List<String>> reasons,
            int gameNumber,
            String numberType
    ) {
        scores.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(numberType.equals("PRIMARY") ? 5 : 1)
                .forEach(entry -> addReason(
                        reasons,
                        entry.getKey(),
                        "Selected for Game " + gameNumber
                                + " because it ranked as a top "
                                + numberType.toLowerCase()
                                + " candidate after the active Strands were blended."));
    }

    private Map<Integer, Double> shiftScores(
            Map<Integer, Double> originalScores,
            int gameNumber,
            String predictionRunId
    ) {
        Map<Integer, Double> shiftedScores = new HashMap<>();
        int runOffset = Math.abs(predictionRunId.hashCode() % 17);

        for (var entry : originalScores.entrySet()) {
            double shift = ((entry.getKey() * gameNumber + runOffset) % 13) * 0.07;
            shiftedScores.put(entry.getKey(), entry.getValue() + shift);
        }

        return shiftedScores;
    }

    private Map<Integer, Double> initializeSignalScores(
            int minValue,
            int maxValue
    ) {
        Map<Integer, Double> signalScores = new HashMap<>();

        for (int value = minValue; value <= maxValue; value++) {
            signalScores.put(value, 1.0);
        }

        return signalScores;
    }

    private void applyIdentitySeedModel(
            Map<Integer, Double> primarySignalScores,
            Map<Integer, Double> bonusSignalScores,
            Map<Integer, List<String>> primarySignalReasons,
            Map<Integer, List<String>> bonusSignalReasons,
            Long userId,
            String email,
            LocalDate predictionDate,
            String predictionRunId
    ) {
        Random random = new Random(createSeed(
                userId,
                email,
                predictionDate,
                predictionRunId + "|identity-seed-model"));

        for (int i = 0; i < 12; i++) {
            int primarySignal = random.nextInt(PRIMARY_SIGNAL_MAX) + PRIMARY_SIGNAL_MIN;

            primarySignalScores.computeIfPresent(
                    primarySignal,
                    (key, value) -> value + 2.5);

            addReason(
                    primarySignalReasons,
                    primarySignal,
                    "Boosted by Identity Seed Model because it matched this user's repeatable identity seed.");
        }

        for (int i = 0; i < 6; i++) {
            int bonusSignal = random.nextInt(BONUS_SIGNAL_MAX) + BONUS_SIGNAL_MIN;

            bonusSignalScores.computeIfPresent(
                    bonusSignal,
                    (key, value) -> value + 2.0);

            addReason(
                    bonusSignalReasons,
                    bonusSignal,
                    "Boosted by Identity Seed Model because it matched this user's repeatable identity seed.");
        }
    }

    private void applyStatisticalSignalModel(
            Map<Integer, Double> primarySignalScores,
            Map<Integer, Double> bonusSignalScores,
            Map<Integer, List<String>> primarySignalReasons,
            Map<Integer, List<String>> bonusSignalReasons
    ) {
        for (int signal = PRIMARY_SIGNAL_MIN; signal <= PRIMARY_SIGNAL_MAX; signal++) {
            double distributionWeight = signal <= 50 ? 1.12 : 1.08;

            primarySignalScores.computeIfPresent(
                    signal,
                    (key, value) -> value * distributionWeight);

            addReason(
                    primarySignalReasons,
                    signal,
                    "Adjusted by Statistical Signal Model for distribution balance.");
        }

        for (int signal = BONUS_SIGNAL_MIN; signal <= BONUS_SIGNAL_MAX; signal++) {
            bonusSignalScores.computeIfPresent(
                    signal,
                    (key, value) -> value * 1.08);

            addReason(
                    bonusSignalReasons,
                    signal,
                    "Adjusted by Statistical Signal Model for bonus-number distribution balance.");
        }
    }

    private void applyBalanceSignalModel(
            Map<Integer, Double> primarySignalScores,
            Map<Integer, List<String>> primarySignalReasons
    ) {
        for (int signal = PRIMARY_SIGNAL_MIN; signal <= PRIMARY_SIGNAL_MAX; signal++) {
            double influenceScore = primarySignalScores.get(signal);

            if (signal >= 10 && signal <= 90) {
                influenceScore += 0.30;
                addReason(
                        primarySignalReasons,
                        signal,
                        "Improved by Balance Signal Model because it falls within the preferred middle range.");
            }

            if (signal % 2 == 0) {
                influenceScore += 0.10;
                addReason(
                        primarySignalReasons,
                        signal,
                        "Slightly boosted by Balance Signal Model for even-number parity.");
            } else {
                influenceScore += 0.15;
                addReason(
                        primarySignalReasons,
                        signal,
                        "Slightly boosted by Balance Signal Model for odd-number parity.");
            }

            primarySignalScores.put(signal, influenceScore);
        }
    }

    private void applyAdaptiveBayesianModel(
            Map<Integer, Double> primarySignalScores,
            Map<Integer, Double> bonusSignalScores,
            Map<Integer, List<String>> primarySignalReasons,
            Map<Integer, List<String>> bonusSignalReasons,
            Long userId,
            String email,
            LocalDate predictionDate,
            String predictionRunId
    ) {
        Random random = new Random(createSeed(
                userId,
                email,
                predictionDate,
                predictionRunId + "|adaptive-bayesian-model"));

        for (int i = 0; i < 10; i++) {
            int primarySignal = random.nextInt(PRIMARY_SIGNAL_MAX) + PRIMARY_SIGNAL_MIN;

            primarySignalScores.computeIfPresent(
                    primarySignal,
                    (key, value) -> value + 1.75);

            addReason(
                    primarySignalReasons,
                    primarySignal,
                    "Boosted by Adaptive Bayesian Model through personal probability weighting.");
        }

        for (int i = 0; i < 5; i++) {
            int bonusSignal = random.nextInt(BONUS_SIGNAL_MAX) + BONUS_SIGNAL_MIN;

            bonusSignalScores.computeIfPresent(
                    bonusSignal,
                    (key, value) -> value + 1.50);

            addReason(
                    bonusSignalReasons,
                    bonusSignal,
                    "Boosted by Adaptive Bayesian Model through personal probability weighting.");
        }
    }

    private void strengthenMonteCarloCandidates(
            Map<Integer, Double> primarySignalScores,
            Map<Integer, List<String>> primarySignalReasons,
            Long userId,
            String email,
            LocalDate predictionDate,
            String predictionRunId
    ) {
        List<Integer> strongestCandidate = runMonteCarloSimulation(
                primarySignalScores,
                userId,
                email,
                predictionDate,
                predictionRunId);

        for (Integer signal : strongestCandidate) {
            primarySignalScores.computeIfPresent(
                    signal,
                    (key, value) -> value + 3.0);

            addReason(
                    primarySignalReasons,
                    signal,
                    "Reinforced by Simulation Model after ranking highly in Monte Carlo candidate testing.");
        }
    }

    private List<Integer> runMonteCarloSimulation(
            Map<Integer, Double> primarySignalScores,
            Long userId,
            String email,
            LocalDate predictionDate,
            String predictionRunId
    ) {
        List<Integer> strongestCandidate = List.of();
        double strongestCandidateScore = -1;

        Random random = new Random(createSeed(
                userId,
                email,
                predictionDate,
                predictionRunId + "|monte-carlo-simulation"));

        for (int i = 0; i < MONTE_CARLO_ITERATIONS; i++) {
            List<Integer> candidate = selectWeightedSignals(
                    primarySignalScores,
                    DEFAULT_PRIMARY_CANDIDATE_COUNT,
                    random.nextLong());

            if (!hasBalancedDistribution(candidate)) {
                continue;
            }

            double candidateScore = candidate.stream()
                    .mapToDouble(primarySignalScores::get)
                    .sum();

            candidateScore -= calculatePatternPenalty(candidate);

            if (candidateScore > strongestCandidateScore) {
                strongestCandidateScore = candidateScore;
                strongestCandidate = candidate;
            }
        }

        if (strongestCandidate.isEmpty()) {
            strongestCandidate = selectWeightedSignals(
                    primarySignalScores,
                    DEFAULT_PRIMARY_CANDIDATE_COUNT,
                    random.nextLong());
        }

        return strongestCandidate;
    }

    private List<Integer> selectWeightedSignals(
            Map<Integer, Double> signalScores,
            int count,
            long seed
    ) {
        Random random = new Random(seed);
        Map<Integer, Double> availableScores = new HashMap<>(signalScores);
        List<Integer> selectedSignals = new ArrayList<>();

        while (selectedSignals.size() < count && !availableScores.isEmpty()) {
            double totalInfluence = availableScores.values()
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            double roll = random.nextDouble() * totalInfluence;
            double runningTotal = 0;

            Iterator<Map.Entry<Integer, Double>> iterator =
                    availableScores.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Integer, Double> entry = iterator.next();

                runningTotal += entry.getValue();

                if (runningTotal >= roll) {
                    selectedSignals.add(entry.getKey());
                    iterator.remove();
                    break;
                }
            }
        }

        return selectedSignals;
    }

    private boolean hasBalancedDistribution(List<Integer> signals) {
        long oddCount = signals.stream()
                .filter(number -> number % 2 != 0)
                .count();

        long lowCount = signals.stream()
                .filter(number -> number <= 50)
                .count();

        return oddCount >= 2
                && oddCount <= 3
                && lowCount >= 2
                && lowCount <= 3;
    }

    private double calculatePatternPenalty(List<Integer> signals) {
        List<Integer> sortedSignals = signals.stream()
                .sorted()
                .toList();

        double penalty = 0;

        for (int i = 1; i < sortedSignals.size(); i++) {
            boolean consecutive =
                    sortedSignals.get(i) - sortedSignals.get(i - 1) == 1;

            if (consecutive) {
                penalty += 0.5;
            }
        }

        int sum = sortedSignals.stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (sum < 90 || sum > 300) {
            penalty += 1.0;
        }

        return penalty;
    }

    private double calculateMatrixScore(
            Map<Integer, Double> primarySignalScores,
            Map<Integer, Double> bonusSignalScores,
            Set<PredictionModel> activePredictionModels
    ) {
        double matrixScore = 50;

        matrixScore += activePredictionModels.size() * 5.0;

        double primaryAverage = primarySignalScores.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1.0);

        double bonusAverage = bonusSignalScores.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1.0);

        matrixScore += Math.min(primaryAverage * 5, 20);
        matrixScore += Math.min(bonusAverage * 3, 10);

        return Math.min(matrixScore, 100);
    }

    private long createSeed(
            Long userId,
            String email,
            LocalDate predictionDate,
            String salt
    ) {
        try {
            String input = userId + "|" + email + "|" + predictionDate + "|" + salt;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            long seed = 0;

            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xff);
            }

            return seed;
        } catch (Exception ex) {
            return Objects.hash(userId, email, predictionDate, salt);
        }
    }

    public List<ArenaSpecification<? extends ArenaRules>> availableArenas() {
        return arenaRegistry.availableArenas();
    }

    private Set<PredictionModel> modelsForGame(
            Set<PredictionModel> enabledModels,
            int gameNumber
    ) {
        Set<PredictionModel> selectedModels = new LinkedHashSet<>();

        if (enabledModels.contains(PredictionModel.DNA_PICK)) {
            selectedModels.add(PredictionModel.DNA_PICK);
        }

        List<PredictionModel> rotatingModels = enabledModels.stream()
                .filter(model -> model != PredictionModel.DNA_PICK)
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        if (!rotatingModels.isEmpty()) {
            int index = Math.floorMod(gameNumber - 1, rotatingModels.size());
            selectedModels.add(rotatingModels.get(index));
        }

        if (gameNumber % 2 == 0 && enabledModels.contains(PredictionModel.BALANCED_PICK)) {
            selectedModels.add(PredictionModel.BALANCED_PICK);
        }

        if (gameNumber % 3 == 0 && enabledModels.contains(PredictionModel.MONTE_CARLO_PICK)) {
            selectedModels.add(PredictionModel.MONTE_CARLO_PICK);
        }

        if (selectedModels.isEmpty()) {
            selectedModels.add(PredictionModel.DNA_PICK);
            selectedModels.add(PredictionModel.BALANCED_PICK);
        }

        return selectedModels;
    }

    private PredictionMatrixResult buildGameMatrix(
            Long userId,
            String email,
            LocalDate effectiveDrawDate,
            String predictionRunId,
            int gameNumber,
            Set<PredictionModel> activePredictionModels,
            String arenaName
    ) {
        List<String> methodsUsed = new ArrayList<>();

        Map<Integer, Double> primarySignalScores = initializeSignalScores(
                PRIMARY_SIGNAL_MIN,
                PRIMARY_SIGNAL_MAX);

        Map<Integer, Double> bonusSignalScores = initializeSignalScores(
                BONUS_SIGNAL_MIN,
                BONUS_SIGNAL_MAX);

        Map<Integer, List<String>> primarySignalReasons = initializeReasonMap(
                PRIMARY_SIGNAL_MIN,
                PRIMARY_SIGNAL_MAX);

        Map<Integer, List<String>> bonusSignalReasons = initializeReasonMap(
                BONUS_SIGNAL_MIN,
                BONUS_SIGNAL_MAX);

        if (activePredictionModels.contains(PredictionModel.DNA_PICK)) {
            applyIdentitySeedModel(
                    primarySignalScores,
                    bonusSignalScores,
                    primarySignalReasons,
                    bonusSignalReasons,
                    userId,
                    email,
                    effectiveDrawDate,
                    predictionRunId + "|game-" + gameNumber);

            methodsUsed.add("Identity Strand™");
        }

        if (activePredictionModels.contains(PredictionModel.STAT_PICK)) {
            applyStatisticalSignalModel(
                    primarySignalScores,
                    bonusSignalScores,
                    primarySignalReasons,
                    bonusSignalReasons);

            methodsUsed.add("Statistics Strand™");
        }

        if (activePredictionModels.contains(PredictionModel.BALANCED_PICK)) {
            applyBalanceSignalModel(primarySignalScores, primarySignalReasons);

            methodsUsed.add("Balance Strand™");
        }

        if (activePredictionModels.contains(PredictionModel.BAYESIAN_PICK)) {
            applyAdaptiveBayesianModel(
                    primarySignalScores,
                    bonusSignalScores,
                    primarySignalReasons,
                    bonusSignalReasons,
                    userId,
                    email,
                    effectiveDrawDate,
                    predictionRunId + "|game-" + gameNumber);

            methodsUsed.add("Adaptive Strand™");
        }

        if (activePredictionModels.contains(PredictionModel.MONTE_CARLO_PICK)) {
            strengthenMonteCarloCandidates(
                    primarySignalScores,
                    primarySignalReasons,
                    userId,
                    email,
                    effectiveDrawDate,
                    predictionRunId + "|game-" + gameNumber);

            methodsUsed.add("Simulation Strand™");
        }

        Map<Integer, Double> gamePrimaryScores = shiftScores(
                primarySignalScores,
                gameNumber,
                predictionRunId);

        Map<Integer, Double> gameBonusScores = shiftScores(
                bonusSignalScores,
                gameNumber,
                predictionRunId);

        Map<Integer, List<String>> gamePrimaryReasons = copyReasonMap(primarySignalReasons);
        Map<Integer, List<String>> gameBonusReasons = copyReasonMap(bonusSignalReasons);

        addShiftReasons(gamePrimaryScores, gamePrimaryReasons, gameNumber, "PRIMARY");
        addShiftReasons(gameBonusScores, gameBonusReasons, gameNumber, "BONUS");

        double matrixScore = calculateMatrixScore(
                gamePrimaryScores,
                gameBonusScores,
                activePredictionModels);

        methodsUsed.add(arenaName);

        return new PredictionMatrixResult(
                gamePrimaryScores,
                gameBonusScores,
                gamePrimaryReasons,
                gameBonusReasons,
                methodsUsed,
                matrixScore
        );
    }
}