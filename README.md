This program implements three numerical methods for finding the roots of nonlinear equations: Newton’s method, the Secant method, and the Bisection method. Each method is applied to the ten functions given in the assignment using the specified initial value x0.

The stopping condition for all three methods is
|xₙ₊₁ − xₙ| < 10⁻⁶
or when the maximum of 100 iterations is reached.

Newton’s method uses the formula
xₙ₊₁ = xₙ − f(xₙ)/f′(xₙ).
The function and its derivative are passed into the method. At each step, the program computes the next approximation and checks the difference between consecutive values. If the derivative becomes zero, the method stops and returns an error message.

The Secant method does not use the derivative. It starts with x0 and a small offset, then applies the secant formula using the two most recent approximations. It also stops when the difference between consecutive approximations is less than 10⁻⁶ or if the denominator becomes zero.

The Bisection method first searches for an interval around x0 where the function changes sign. Once a valid interval is found, it repeatedly halves the interval and selects the subinterval where a sign change occurs. It stops when the difference between two consecutive midpoints is less than 10⁻⁶.

For each problem and each method, the program prints the root found, the number of iterations used, and a message indicating whether the method converged or failed. This allows comparison of how quickly and reliably each method finds the root.

In general, Newton’s method converges the fastest when the derivative behaves well. The Secant method is slightly slower but does not require a derivative. The Bisection method is slower but more reliable because it guarantees convergence when a sign change exists.
