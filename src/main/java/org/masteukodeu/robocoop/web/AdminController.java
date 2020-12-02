package org.masteukodeu.robocoop.web;

import org.masteukodeu.robocoop.db.*;
import org.masteukodeu.robocoop.model.Cart;
import org.masteukodeu.robocoop.model.Category;
import org.masteukodeu.robocoop.model.Order;
import org.masteukodeu.robocoop.model.Round;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final RoundDAO roundDAO;
    private final ConfigDAO configDAO;
    private final CategoryDAO categoryDAO;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public AdminController(RoundDAO roundDAO, ConfigDAO configDAO, CategoryDAO categoryDAO, OrderDAO orderDAO, ProductDAO productDAO) {
        this.roundDAO = roundDAO;
        this.configDAO = configDAO;
        this.categoryDAO = categoryDAO;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    @GetMapping("/admin/new_round")
    public String newRoundForm() {
        return "admin/new_round";
    }

    @PostMapping("/admin/new_round")
    public String createNewRound(@RequestParam("round_name") String roundName, @RequestParam("final_date") String finalDate) {
        String roundId = roundDAO.add(new Round(null, roundName, LocalDate.parse(finalDate)));
        configDAO.setCurrentRound(roundId);
        return "redirect:/admin/new_round_created";
    }

    @GetMapping("/admin/new_round_created")
    public String newRoundCreated() {
        return "admin/new_round_created";
    }


    @GetMapping("/admin")
    public String admin() {
        return "admin/admin";
    }

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryDAO.all());
        return "admin/categories";
    }

    @GetMapping("/admin/category/edit")
    public String categoryEditForm(Model model, String id) {
        model.addAttribute("category", categoryDAO.byId(id));
        return "admin/category_edit";
    }

    @PostMapping("/admin/category/edit")
    public String categoryEdit(String id, String name, boolean hidden, @RequestParam("blocked_period") BigDecimal blockedPeriod) {
        Category category = new Category(id, name, hidden, blockedPeriod);
        categoryDAO.update(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/history")
    public String history(Model model) {
        model.addAttribute("history", roundDAO.all());
        return "admin/history";
    }

    @GetMapping("/admin/round")
    public String roundDetails(Model model, @RequestParam("id") String roundId) {
        model.addAttribute("round", roundDAO.byId(roundId));
        List<Order> orders = orderDAO.byRound(roundId);
        List<Cart.Item> cartItems = orders.stream().map(o -> new Cart.Item(o.getId(), productDAO.byId(o.getProductId()), o.getQuantity())).collect(Collectors.toList());
        model.addAttribute("orders", cartItems);
        return "admin/round_details";
    }
}

