package com.api.parkingcontrol.controllers;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    @Autowired
    ParkingSpotService parkingSpotService;

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto){ // json -- @Valid faz as validações do DTO
        if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Vaga já ocupada.");
        }
        if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Número da vaga ocupado.");
        }
        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este apartamento já tem um carro cadastrado.");
        }


        var parkingSpotModel = new ParkingSpotModel(); // dentro do escopo, eu posso usar VAR, ao invés, de ParkingSpotModel
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel); // converte o DTO, para MODEL. O primeiro parametro, é de onde ele vai ser convertido, e o segundo, para o que ele vai ser convertido, para poder salvar no banco de dados
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC"))); // faz com que a data, seja posta automaticamente
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return  ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()) { // verifica se não esta presente
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    /* @GetMapping("/{modelcar}")
    public ResponseEntity<Object> getModelCar(@PathVariable(value = "modelCar") String modelCar){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findByModel(modelCar);
        if(!parkingSpotModelOptional.isPresent()) { // verifica se não esta presente
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Modelo não encontrada");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    } */

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()) { // verifica se não esta presente
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada");
        }
        parkingSpotService.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Vaga deletada com sucesso");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,
                                                                @RequestBody @Valid ParkingSpotDto parkingSpotDto){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()) { // verifica se não esta presente
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada");
        }

        /*
            var parkingSpotModel = parkingSpotModelOptional.get();
                        // QUAIS DADOS PODEM SER ATUALIZADOS:
            parkingSpotModel.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
            parkingSpotModel.setLicensePlateCar(parkingSpotDto.getLicensePlateCar());
            parkingSpotModel.setModelCar(parkingSpotDto.getModelCar());
            parkingSpotModel.setBrandCar(parkingSpotDto.getBrandCar());
            parkingSpotModel.setColorCar(parkingSpotDto.getColorCar());
            parkingSpotModel.setResponsibleName(parkingSpotDto.getResponsibleName());
            parkingSpotModel.setApartment(parkingSpotDto.getApartment());
            parkingSpotModel.setBlock(parkingSpotDto.getBlock());
        */
        // OU
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        // ESSES CAMPOS EU MANTENHO.
        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());


        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    }
}
