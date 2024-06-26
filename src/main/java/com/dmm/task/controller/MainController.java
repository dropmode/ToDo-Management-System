package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.entity.Tasks;
import com.dmm.task.form.TaskForm;
import com.dmm.task.repository.TasksRepository;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class MainController {

	@Autowired
	private TasksRepository repo;

	// カレンダー表示用
	@GetMapping("/main")
	public String main(Model model, @AuthenticationPrincipal AccountUserDetails user) {

		// 週と日を格納する二次元配列を用意する
		List<List<LocalDate>> month = new ArrayList<>();

		// 1週間分のLocalDateを格納するListを用意する
		List<LocalDate> week = new ArrayList<>();

		// 日にちを格納する変数を用意する
		LocalDate day, start, end;

		day = LocalDate.now(); // 現在日時を取得
		day = LocalDate.of(day.getYear(), day.getMonthValue(), 1); // 現在日時からその月の1日を取得

		// 前月分の LocalDateを求める
		DayOfWeek w = day.getDayOfWeek(); // 当該日の曜日を取得
		day = day.minusDays(w.getValue()); // 1日からマイナスして 5/26を取得
		start = day;

		// 1週目（1日ずつ増やして 週のリストに格納していく）
		for (int i = 1; i <= 7; i++) {
			week.add(day); // 週のリストへ格納
			day = day.plusDays(1); // 1日進める
		}
		month.add(week); // 1週目のリストを、月のリストへ格納する

		week = new ArrayList<>(); // 次週のリストを新しくつくる

		// 2週目（「7」から始めているのは2週目だから）
		for (int i = 7; i <= day.lengthOfMonth(); i++) {
			week.add(day); // 週のリストへ格納

			w = day.getDayOfWeek();
			if (w == DayOfWeek.SATURDAY) { // 土曜日だったら
				month.add(week); // 当該週のリストを、月のリストへ格納する
				week = new ArrayList<>(); // 次週のリストを新しくつくる
			}

			day = day.plusDays(1); // 1日進める
		}
		// 最終週の翌月分
		w = day.getDayOfWeek();
		for (int i = 1; i < 7 - w.getValue(); i++) {
			week.add(day);
			day = day.plusDays(1);
		}
		end = day;

		// 日付とタスクを紐付けるコレクション
		MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();

		// リポジトリからタスクを取得
		List<Tasks> list;
		if (user.getUsername().equals("admin")) {
			// 管理者だったら
			list = repo.findAllByDateBetween(start.atTime(0, 0), end.atTime(0, 0));
		} else {
			// ユーザーだったら
			list = repo.findByDateBetween(start.atTime(0, 0), end.atTime(0, 0), user.getName());
		}

		// ★取得したタスクをコレクションに追加
		for (Tasks task : list) {
			tasks.add(task.getDate().toLocalDate(), task);
		}

		// ★コレクションのデータをHTMLに連携
		model.addAttribute("tasks", tasks);

		// ★カレンダーのデータをHTMLに連携
		model.addAttribute("matrix", month);

		// コレクションのデータをHTMLに連携
		model.addAttribute("tasks", tasks);

		return "main";
	}

	// タスク登録画面の表示用
	@GetMapping("/main/create/{date}")
	public String create(Model model, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

		return "create";
	}

	// タスク登録用
	@PostMapping("/main/create")
	public String createPost(Model model, TaskForm form, @AuthenticationPrincipal AccountUserDetails user) {
		Tasks task = new Tasks();
		task.setName(user.getName());
		task.setTitle(form.getTitle());
		task.setText(form.getText());
		task.setDate(form.getDate().atTime(0, 0));

		repo.save(task);

		return "redirect:/main";

	}

}
