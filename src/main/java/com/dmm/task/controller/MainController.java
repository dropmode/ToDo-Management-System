package com.dmm.task.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping("/main")
	public String main() {

		// 週と日を格納する二次元配列を用意する
		List<List<LocalDate>> month = new ArrayList<>();

		return "main";
	}

}
