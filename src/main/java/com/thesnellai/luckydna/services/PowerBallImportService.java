package com.thesnellai.luckydna.services;

import com.thesnellai.luckydna.dto.PowerBallImportRow;
import com.thesnellai.luckydna.models.PowerBallDrawing;
import com.thesnellai.luckydna.repositories.PowerBallDrawingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Arrays;

@Service
public class PowerBallImportService {
    private static final String POWERBALL_URL =
            "https://data.ny.gov/resource/d6yy-54nr.json?$limit=5000";

    private final PowerBallDrawingRepository repository;
    private final RestClient restClient;
    private final PlayResultService playResultService;

    public PowerBallImportService(
            PowerBallDrawingRepository repository,
            PlayResultService playResultService
    ) {
        this.repository = repository;
        this.playResultService = playResultService;
        this.restClient = RestClient.create();
    }

    public ImportResult sync() {
        var rows = restClient.get()
                .uri(POWERBALL_URL)
                .retrieve()
                .body(PowerBallImportRow[].class);

        if (rows == null) {
            return new ImportResult(0, 0, 0);
        }

        int inserted = 0;
        int resultsCreated = 0;

        for (var row : rows) {
            if (row.drawDate() == null || row.winningNumbers() == null) {
                continue;
            }

            var drawDate = LocalDate.parse(row.drawDate().substring(0, 10));

            if (repository.existsByDrawingDate(drawDate)) {
                continue;
            }

            var numbers = Arrays.stream(row.winningNumbers().split(" "))
                    .filter(s -> !s.isBlank())
                    .map(Integer::parseInt)
                    .toList();

            if (numbers.size() < 6) {
                continue;
            }

            var drawing = new PowerBallDrawing();
            drawing.setDrawingDate(drawDate);
            drawing.setWhiteBall1(numbers.get(0));
            drawing.setWhiteBall2(numbers.get(1));
            drawing.setWhiteBall3(numbers.get(2));
            drawing.setWhiteBall4(numbers.get(3));
            drawing.setWhiteBall5(numbers.get(4));
            drawing.setPowerBall(numbers.get(5));

            var savedDrawing = repository.save(drawing);
            inserted++;

            resultsCreated += playResultService.checkDrawingAgainstAllSavedPlays(savedDrawing);
        }

        return new ImportResult(rows.length, inserted, resultsCreated);
    }

    public record ImportResult(
            int rowsRead,
            int inserted,
            int resultsCreated
    ) {}
}