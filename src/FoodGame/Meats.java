/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FoodGame;

/**
 *
 * @author nate
 */
public class Meats extends FoodType {
    
   public Meats(String name, int cal, float fat, float sugar, float carbs, float protein)
   {
       this.vegan = false;
       this.vegitarian = false;
       this.Name = name;
       this.Calories = cal;
       this.Fat = fat;
       this.sugars=sugar;
       this.proteins=protein;
       this.carbs=carbs;
       
   }
  
 
    
}
