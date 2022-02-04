package com.dwu.alonealong.controller;

import javax.servlet.http.HttpServletRequest;

import com.dwu.alonealong.exception.NullProductException;
import com.dwu.alonealong.exception.StockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.dwu.alonealong.domain.CartItem;
import com.dwu.alonealong.domain.Product;
import com.dwu.alonealong.service.AloneAlongFacade;
import com.dwu.alonealong.exception.UserNotMatchException;

@Controller
@SessionAttributes({"userSession"})
public class CartItemController {
	private AloneAlongFacade aloneAlong;

	@Autowired
	public void setAloneAlong(AloneAlongFacade aloneAlong) {
		this.aloneAlong = aloneAlong;
	}

	@RequestMapping("/cart/delete")
	public String deleteCartItem(HttpServletRequest request,
			@RequestParam(value="cartItemId") String cartItemId,
			ModelMap model) throws Exception {
		UserSession userSession = (UserSession)request.getSession().getAttribute("userSession");
		if(userSession == null) {
			return "redirect:/login";
		}

		CartItem cartItem = aloneAlong.getCartItem(cartItemId);
		if(cartItem == null){
			throw new NullProductException();
		}
		if(!userSession.getUser().getId().equals(cartItem.getUserId())) {
			throw new UserNotMatchException();
		}
		this.aloneAlong.deleteCartItem(cartItemId);
		
		return "redirect:/cart";
	}

	@RequestMapping("/cart/update/{cartItemId}")
	public String updateCartItem(HttpServletRequest request,
			@PathVariable("cartItemId") String cartItemId,
			@RequestParam(value="quantity") int quantity,
			ModelMap model) throws Exception {
		UserSession userSession = (UserSession)request.getSession().getAttribute("userSession");
		if(userSession == null) {
			return "redirect:/login";
		}
		CartItem cartItem = aloneAlong.getCartItem(cartItemId);
		if(cartItem == null){
			throw new NullProductException();
		}
		if(!userSession.getUser().getId().equals(cartItem.getUserId())) {
			throw new UserNotMatchException();
		}

		Product product = aloneAlong.getProduct(cartItem.getProductId());
		cartItem.setQuantity(quantity);
		int stock = product.getProductStock();
		if(stock < quantity){
			return "redirect:/cart?stockError=true&productId=" + cartItem.getProductId() + "&stock=" + stock;
		}
		aloneAlong.updateCartItem(cartItem);
		return "redirect:/cart";
	}
	


	@RequestMapping("/cart/insert/{productId}/{type}")
	public String insertCartItem(HttpServletRequest request,
			@PathVariable("productId") String productId,
			@PathVariable("type") String type, 
			@RequestParam(value="quantity", defaultValue="1") int quantity, 
			@RequestParam(value="page", defaultValue="1") String page,
			@RequestParam(value="sortType", required=false) String sortType,
			ModelMap model) throws Exception {
		UserSession userSession = (UserSession)request.getSession().getAttribute("userSession");
		if(userSession == null) {
			return "redirect:/login";
		}
		String userId = userSession.getUser().getId();
		Product product = aloneAlong.getProduct(productId);
		int stock = product.getProductStock();
		
		product.setQuantity(quantity);
		model.put("product", product);
		model.put("pcId", product.getPcId());
		
		if(type.equals("list")) {
			if(stock < quantity){
				throw new StockException();
			}
			aloneAlong.insertCartItem(productId, quantity, userId);
			return "redirect:/shop?insertCart=true&pcId=" + product.getPcId() + "&page=" + page + "&sortType=" + sortType;
		}
		else if(type.equals("product")) {
			if(stock < quantity){
				throw new StockException();
			}
			aloneAlong.insertCartItem(productId, quantity, userId);
			return "redirect:/shop/" + productId + "?insertCart=true";
		}
		throw new Exception("올바르지 않은 타입입니다.");
	}
}
