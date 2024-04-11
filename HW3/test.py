from sklearn.linear_model import LinearRegression
import numpy as np


# double[][] X = {{1, 1, 1, 1, 1}, {12d, 131d, 11d, 345d, 74d}};
# double[][] y = {{4d, 5d, 12d, 4d, 65d}};

x = np.array([[1, 1, 1, 1, 1], [12, 131, 11, 345, 74]])
y = np.array([4, 5, 12, 4, 65])

model = LinearRegression()
model.fit(x.T, y.T)

print(model.coef_, model.intercept_)
