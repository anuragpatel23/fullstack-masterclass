/**
 * TOPIC: Lifecycle — the same component written as a class and with hooks,
 * side by side, plus an error boundary (still class-only).
 */
import React, { useState, useEffect, useRef } from 'react';

// =====================================================================
// 1. CLASS version — annotate the lifecycle order out loud
// =====================================================================
export class StockTickerClass extends React.Component {
  constructor(props) {
    super(props);                                   // ALWAYS first
    this.state = { price: null, seconds: 0 };       // state init
    console.log('1. constructor');
  }

  componentDidMount() {                             // after first DOM commit
    console.log('3. componentDidMount (render was 2)');
    this.timer = setInterval(
      () => this.setState(s => ({ seconds: s.seconds + 1 })), 1000);
    this.fetchPrice(this.props.symbol);             // fetch here, never in constructor
  }

  componentDidUpdate(prevProps) {                   // every update after render
    if (prevProps.symbol !== this.props.symbol) {   // ⭐ ALWAYS compare, or infinite loop
      this.fetchPrice(this.props.symbol);
    }
  }

  shouldComponentUpdate(nextProps, nextState) {     // perf gate (React.memo ancestor)
    return nextProps.symbol !== this.props.symbol
        || nextState.price !== this.state.price
        || nextState.seconds !== this.state.seconds;
  }

  componentWillUnmount() {                          // cleanup or leak ⭐
    console.log('componentWillUnmount');
    clearInterval(this.timer);
  }

  fetchPrice(symbol) {
    Promise.resolve(100 + Math.random() * 10)       // fake API
      .then(price => this.setState({ price: price.toFixed(2) }));
  }

  render() {                                        // pure: no side effects here!
    console.log('2. render');
    return <p>{this.props.symbol}: ₹{this.state.price} (watched {this.state.seconds}s)</p>;
  }
}

// =====================================================================
// 2. HOOKS version — same behavior, concerns SEPARATED ⭐⭐
// =====================================================================
export function StockTickerHooks({ symbol }) {
  const [price, setPrice] = useState(null);         // constructor + this.state
  const [seconds, setSeconds] = useState(0);

  // Concern A: timer. mount+unmount only ([] deps) = didMount + willUnmount
  useEffect(() => {
    const timer = setInterval(() => setSeconds(s => s + 1), 1000);
    return () => clearInterval(timer);              // cleanup ⭐
  }, []);

  // Concern B: fetch on symbol change = didMount + didUpdate-with-compare, in ONE place
  useEffect(() => {
    let cancelled = false;                          // race-condition guard ⭐
    Promise.resolve(100 + Math.random() * 10).then(p => {
      if (!cancelled) setPrice(p.toFixed(2));       // ignore stale responses
    });
    return () => { cancelled = true; };             // cleanup runs BEFORE next run too ⭐⭐
  }, [symbol]);

  return <p>{symbol}: ₹{price} (watched {seconds}s)</p>;
}

// =====================================================================
// 3. "componentDidUpdate ONLY" with hooks (skip the mount run)
// =====================================================================
export function UpdateOnlyLogger({ value }) {
  const isFirstRender = useRef(true);
  useEffect(() => {
    if (isFirstRender.current) {                    // guard: skip mount
      isFirstRender.current = false;
      return;
    }
    console.log('value UPDATED to', value);         // true didUpdate semantics
  }, [value]);
  return <span>{value}</span>;
}

// =====================================================================
// 4. ERROR BOUNDARY — still class-only ⭐
// =====================================================================
export class ErrorBoundary extends React.Component {
  state = { error: null };

  static getDerivedStateFromError(error) {          // render the fallback
    return { error };
  }
  componentDidCatch(error, info) {                  // side effects: log to Sentry etc.
    console.error('boundary caught:', error, info.componentStack);
  }
  render() {
    if (this.state.error) {
      return (
        <div role="alert">
          <p>Something broke: {this.state.error.message}</p>
          <button onClick={() => this.setState({ error: null })}>Try again</button>
        </div>
      );
    }
    return this.props.children;
  }
}
// Limits ⭐: does NOT catch event-handler errors, async/promise errors,
// SSR errors, or its own errors. Wrap risky subtrees:
//   <ErrorBoundary><PaymentWidget /></ErrorBoundary>

/*
 * SIDE-BY-SIDE SUMMARY:
 * class: concerns SCATTERED across lifecycle methods (timer setup in didMount,
 *        teardown far away in willUnmount; fetch split between didMount+didUpdate).
 * hooks: each concern is ONE effect with its own deps + cleanup — colocated.
 * That colocation IS the reason hooks replaced lifecycles — say exactly this.
 */
