package com.dwu.alonealong.controller.restProduct;

import com.dwu.alonealong.exception.NullProductException;
import com.dwu.alonealong.exception.UserNotMatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dwu.alonealong.domain.CartItem;
import com.dwu.alonealong.service.AloneAlongFacade;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@SessionAttributes({"userSession"})
public class RestCartController {
    private AloneAlongFacade aloneAlong;

    @Autowired
    public void setAloneAlong(AloneAlongFacade aloneAlong) {
        this.aloneAlong = aloneAlong;
    }

    @GetMapping("/cart/{userId}/items")
    public ResponseEntity<List<CartItem>> ViewCartItemList(@PathVariable("userId") String userId){
        return ResponseEntity.ok(aloneAlong.getAllCartItem(userId));
    }

    @DeleteMapping("/cart/{userId}/items")
    public ResponseEntity<Void> DeleteCartItemList(@PathVariable("userId") String userId){
        try{
            aloneAlong.deleteAllCartItem(userId);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/{userId}/items")
    public ResponseEntity<Void> addCartItem(@RequestBody CartItem cartItem,
                                   @PathVariable("userId") String userId){
        if(userId.isEmpty()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "로그인이 필요합니다.");
        }
        try{
            aloneAlong.insertCartItem(cartItem.getProductId(), cartItem.getQuantity(), userId);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/cart/{userId}/items/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(@RequestBody CartItem cartItem,
                            @PathVariable("userId") String userId,
                            @PathVariable("cartItemId") long cartItemId) throws Exception {
        CartItem nowCartItem = aloneAlong.getCartItem(cartItemId);
        if(cartItem == null){
            throw new NullProductException();
        }
        if(!userId.equals(nowCartItem.getUserId())) {
            throw new UserNotMatchException();
        }
        nowCartItem.setQuantity(cartItem.getQuantity());
        aloneAlong.updateCartItem(nowCartItem);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart/{userId}/items/{cartItemId}")
    public void DeleteCartItem(@PathVariable("userId") String userId,
                               @PathVariable("cartItemId") long cartItemId){
        aloneAlong.deleteCartItem(cartItemId);
    }
}
