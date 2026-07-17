/**
 * TOPIC: Prototypes & Classes — chain mechanics, hand-made `new`,
 * both inheritance styles, private fields. Run: node prototypes.js
 */

// ===== 1. The chain, visibly ⭐ =====
const animal = { eats: true, describe() { return `eats:${this.eats}`; } };
const rabbit = Object.create(animal);              // rabbit --> animal --> Object.prototype --> null
rabbit.hops = true;

console.log(rabbit.eats);                          // true — found up the chain (delegation, no copy)
console.log(Object.hasOwn(rabbit, 'eats'));        // false — not its own ⭐
console.log('eats' in rabbit);                     // true  — `in` includes inherited
console.log(Object.getPrototypeOf(rabbit) === animal);            // true
console.log(Object.getPrototypeOf(Object.prototype));             // null — end of chain

// ===== 2. prototype vs __proto__ ⭐⭐ =====
function Car(model) { this.model = model; }
Car.prototype.drive = function () { return `${this.model} vroom`; };  // SHARED method

const bmw = new Car('BMW');
console.log(Object.getPrototypeOf(bmw) === Car.prototype);        // true — THE relationship
console.log(bmw.drive());                          // found on Car.prototype
const audi = new Car('Audi');
console.log(bmw.drive === audi.drive);             // true — ONE copy for all instances ⭐

// ===== 3. Implement `new` yourself ⭐ =====
function myNew(Fn, ...args) {
  const obj = Object.create(Fn.prototype);         // steps 1+2: create + link
  const result = Fn.apply(obj, args);              // step 3: run constructor with this=obj
  return typeof result === 'object' && result !== null ? result : obj;  // step 4
}
const tesla = myNew(Car, 'Tesla');
console.log(tesla.drive(), tesla instanceof Car);  // "Tesla vroom" true

// ===== 4. Pre-ES6 inheritance (recognize in output questions) =====
function Animal(name) { this.name = name; }
Animal.prototype.speak = function () { return `${this.name} makes a sound`; };

function Dog(name) {
  Animal.call(this, name);                         // super(name) equivalent
}
Dog.prototype = Object.create(Animal.prototype);   // Dog.prototype --> Animal.prototype
Dog.prototype.constructor = Dog;                   // repair constructor pointer
Dog.prototype.speak = function () {                // override
  return Animal.prototype.speak.call(this) + ' — woof!';
};
console.log(new Dog('Rex').speak());               // "Rex makes a sound — woof!"

// ===== 5. Same thing with ES6 class (sugar over #4) =====
class AnimalC {
  #vaccinated = false;                             // real private field ⭐
  constructor(name) { this.name = name; }
  speak() { return `${this.name} makes a sound`; }
  static kingdom() { return 'Animalia'; }          // on the class, not instances
  get status() { return this.#vaccinated ? 'safe' : 'pending'; }
  vaccinate() { this.#vaccinated = true; }
}

class DogC extends AnimalC {
  constructor(name, breed) {
    super(name);                                   // MUST come before `this` ⭐
    this.breed = breed;
  }
  speak() { return super.speak() + ' — woof!'; }   // override + super call
}

const rex = new DogC('Rex', 'GSD');
rex.vaccinate();
console.log(rex.speak(), '|', rex.status, '|', DogC.kingdom());
console.log(rex instanceof DogC, rex instanceof AnimalC);          // true true — chain walk
// console.log(rex.#vaccinated);                   // SyntaxError — genuinely private ⭐

// ===== 6. Gotchas =====
console.log(typeof DogC);                          // "function" — classes ARE functions
// DogC();                                         // TypeError: class requires `new`

const dict = Object.create(null);                  // prototype-less: pure dictionary
dict.toString = 'safe!';                           // no inherited collisions
console.log('null-proto dict:', dict.toString);

// for...in walks inherited enumerables — the filter idiom:
for (const key in rabbit) {
  if (Object.hasOwn(rabbit, key)) console.log('own key only:', key);   // hops (not eats)
}
