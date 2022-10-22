package com.example.profebotia;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controller {

    @RequestMapping(method = RequestMethod.GET)
    public String Status() {
        return "Ok!";
    }

    @RequestMapping(value = "/api", method = RequestMethod.GET)
    public List<String> newEquationsSimilarTo() {
        return GeneticAlgorithmExecutor.execute("3(x+1)", "3(x+1)+5", "=").getEquations();
    }

    @RequestMapping(value = "/api/more/practice", method = RequestMethod.POST)
    public List<String> newEquationsSimilarTo(@RequestBody IAModuleParams params) {
        return GeneticAlgorithmExecutor.execute(params.getTerm(), params.getContext(), params.getRoot()).getEquations();
    }
}
