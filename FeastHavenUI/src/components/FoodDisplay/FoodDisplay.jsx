import React, { useContext } from 'react'
import { StoreContext } from '../../context/StoreContext'
import FoodItem from '../FoodItem/FoodItem';

const FoodDisplay = ({category,searchText}) => {

  const {foodList} = useContext(StoreContext);
  const trimmedSearchText = searchText.trim().toLowerCase();
  const filteredFoods = foodList.filter(
    (food) =>
      (category === "All" || food.category === category) &&
      food.name.toLowerCase().includes(trimmedSearchText)
  );
  return (
    <div className="container">
      <div className="row">
        {filteredFoods.length > 0 ? (
          filteredFoods.map((food, index) => (
            <FoodItem
              key={index}
              name={food.name}
              description={food.description}
              id={food.id}
              imageUrl={food.imageUrl}
              imageBase64={food.imageBase64}
              price={food.price}
            />
          ))
        ) : (
          <div className="text-center mt-4">
            <h4>No food item found</h4>
          </div>
        )}
      </div>
    </div>
  );
}

export default FoodDisplay;
