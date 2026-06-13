package com.thesnellai.luckydna.controllers;

import com.thesnellai.luckydna.dto.DrawingRequest;
import com.thesnellai.luckydna.dto.DrawingResponse;
import com.thesnellai.luckydna.models.PowerBallDrawing;
import com.thesnellai.luckydna.repositories.PowerBallDrawingRepository;
import com.thesnellai.luckydna.services.PowerBallImportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drawings")
public class DrawingController {
    private final PowerBallDrawingRepository repository;
    private final PowerBallImportService importService;

    public DrawingController(
            PowerBallDrawingRepository repository,
            PowerBallImportService importService
    ) {
        this.repository = repository;
        this.importService = importService;
    }
    @GetMapping
    public List<DrawingResponse> getDrawings() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public DrawingResponse createDrawing(@RequestBody DrawingRequest request) {
        var drawing = new PowerBallDrawing();
        drawing.setDrawingDate(request.drawingDate());
        drawing.setWhiteBall1(request.whiteBall1());
        drawing.setWhiteBall2(request.whiteBall2());
        drawing.setWhiteBall3(request.whiteBall3());
        drawing.setWhiteBall4(request.whiteBall4());
        drawing.setWhiteBall5(request.whiteBall5());
        drawing.setPowerBall(request.powerBall());

        return toResponse(repository.save(drawing));
    }

    private DrawingResponse toResponse(PowerBallDrawing drawing) {
        return new DrawingResponse(
                drawing.getId(),
                drawing.getDrawingDate(),
                drawing.getWhiteBall1(),
                drawing.getWhiteBall2(),
                drawing.getWhiteBall3(),
                drawing.getWhiteBall4(),
                drawing.getWhiteBall5(),
                drawing.getPowerBall()
        );
    }

    @PostMapping("/sync")
    public PowerBallImportService.ImportResult syncDrawings() {
        return importService.sync();
    }
}
