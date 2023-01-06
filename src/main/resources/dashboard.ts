const aggregatorDashboard = (p: p5) => {
  let daysQueryResponse = [...Array(7).keys()].map((day) => {
    interval().epochTime(epochTime());
  });

  class Grid {
    private constructor(
      //
      public readonly border: number,
      public readonly tickPixelWidth: number
    ) {}

    static resize(p: p5): Grid {
      const border = 20;
      const tickWidth = (p.windowWidth - 2 * border) / 16; // aspect ratio 16:9
      const tickHeight = (p.windowHeight - 2 * border) / 9;
      return new Grid(border, Math.min(tickWidth, tickHeight));
    }

    toGridX(x: number): number {
      return this.border + x * this.tickPixelWidth;
    }

    toGridY(y: number): number {
      return this.border + y * this.tickPixelWidth;
    }

    toGridW(w: number): number {
      return w * this.tickPixelWidth;
    }

    toGridH(h: number): number {
      return h * this.tickPixelWidth;
    }
  }

  let grid: Grid = Grid.resize(p);

  class Label {
    constructor(
      //
      private _text: string = '',
      private _x: number = 0,
      private _y: number = 0,
      private _w: number = 0,
      private _h: number = 0,
      private _border: number = 0,
      private _bgColor: p5.Color = p.color(0, 0, 0, 0),
      private _textColor: p5.Color = p.color(255, 255, 255),
      private _textAlign: [p5.HORIZ_ALIGN, p5.VERT_ALIGN] = ['left', 'alphabetic'],
      private _grid: Grid = grid
    ) {}

    static label(): Label {
      return new Label();
    }

    text(text: string): Label {
      this._text = text;
      return this;
    }

    x(x: number): Label {
      this._x = this._grid.toGridX(x);
      return this;
    }

    y(y: number): Label {
      this._y = this._grid.toGridY(y);
      return this;
    }

    w(w: number): Label {
      this._w = this._grid.toGridW(w);
      return this;
    }

    h(h: number): Label {
      this._h = this._grid.toGridH(h);
      return this;
    }

    border(border: number): Label {
      this._border = this._grid.toGridW(border);
      return this;
    }

    pixelX(x: number): Label {
      this._x = x;
      return this;
    }

    pixelY(y: number): Label {
      this._y = y;
      return this;
    }

    pixelW(w: number): Label {
      this._w = w;
      return this;
    }

    pixelH(h: number): Label {
      this._h = h;
      return this;
    }

    bgColor(bgColor: p5.Color): Label {
      this._bgColor = bgColor;
      return this;
    }

    textColor(textColor: p5.Color): Label {
      this._textColor = textColor;
      return this;
    }

    textAlign(horizontal: p5.HORIZ_ALIGN, vertical?: p5.VERT_ALIGN): Label {
      this._textAlign = [horizontal, vertical ?? 'alphabetic'];
      return this;
    }

    draw(p: p5): void {
      p.stroke(0);
      p.fill(this._bgColor);
      p.rect(this._x, this._y, this._w, this._h);

      p.fill(this._textColor);
      p.textSize(this._h - 2 * this._border);
      p.textAlign(this._textAlign[0] as p5.HORIZ_ALIGN, this._textAlign[1] as p5.VERT_ALIGN);
      p.text(this._text, this._x + this._border, this._y + this._border, this._w - 2 * this._border, this._h - 2 * this._border);
    }
  }

  class Labels {
    private constructor(
      //
      private _x: number = 0,
      private _y: number = 0,
      private _w: number = 0,
      private _h: number = 0,
      private _cols: number = 0,
      private _label: Label = label(),
      private _payloads: Payload[] = []
    ) {}

    static labels() {
      return new Labels();
    }

    addAll(payloads: Payload[]): Labels {
      this._payloads.push(...payloads);
      return this;
    }

    x(x: number): Labels {
      this._x = x;
      return this;
    }

    y(y: number): Labels {
      this._y = y;
      return this;
    }

    w(w: number): Labels {
      this._w = w;
      return this;
    }

    h(h: number): Labels {
      this._h = h;
      return this;
    }

    cols(cols: number): Labels {
      this._cols = cols;
      return this;
    }

    label(label: Label): Labels {
      this._label = label;
      return this;
    }

    draw(p: p5): void {
      Array(this._payloads.length / this._cols).forEach((row) => {
        Array(this._cols).forEach((col) => {
          const payload = this._payloads[row * this._cols + col];
          this._label.x(this._x + col);
          this._label.y(this._y + row);
          this._label.text(payload.amount.toLocaleString());
          this._label.draw(p);
        });
      });
    }
  }

  class DataLabel extends Label {
    private constructor(
      //
      private _hour: number = 0
    ) {
      super();
    }

    static hourLabel(hour: number): DataLabel {
      return new DataLabel();
    }

    draw(p: p5): void {
      this.x(this._hour).y(-1).w(1).h(1);
      super.draw(p);
    }
  }

  const label = () => Label.label();

  class Point {
    private constructor(
      //
      private _x: number = 0,
      private _y: number = 0,
      private _weight: number = 1,
      private _color: p5.Color = p.color(255, 255, 255),
      private _grid: Grid = grid
    ) {}

    static point() {
      return new Point();
    }

    x(x: number): Point {
      this._x = this._grid.toGridX(x);
      return this;
    }

    y(y: number): Point {
      this._y = this._grid.toGridY(y);
      return this;
    }

    weight(weight: number): Point {
      this._weight = this._grid.toGridW(weight);
      return this;
    }

    color(color: p5.Color): Point {
      this._color = color;
      return this;
    }

    draw(p: p5): void {
      p.stroke(this._color);
      p.strokeWeight(this._weight);
      p.point(this._x, this._y);
    }
  }

  const point = () => Point.point();

  class Line {
    private constructor(
      //
      private _x1: number = 0,
      private _y1: number = 0,
      private _x2: number = 0,
      private _y2: number = 0,
      private _weight: number = 1,
      private _color: p5.Color = p.color(255, 255, 255),
      private _grid: Grid = grid
    ) {}

    static line() {
      return new Line();
    }

    x1(x1: number): Line {
      this._x1 = this._grid.toGridX(x1);
      return this;
    }

    y1(y1: number): Line {
      this._y1 = this._grid.toGridY(y1);
      return this;
    }

    x2(x2: number): Line {
      this._x2 = this._grid.toGridX(x2);
      return this;
    }

    y2(y2: number): Line {
      this._y2 = this._grid.toGridY(y2);
      return this;
    }

    weight(weight: number): Line {
      this._weight = this._grid.toGridW(weight);
      return this;
    }

    color(color: p5.Color): Line {
      this._color = color;
      return this;
    }

    draw(p: p5): void {
      p.stroke(this._color);
      p.strokeWeight(this._weight);
      p.line(this._x1, this._y1, this._x2, this._y2);
    }
  }

  const line = () => Line.line();

  class Rect {
    private constructor(
      //
      private _x: number = 0,
      private _y: number = 0,
      private _w: number = 0,
      private _h: number = 0,
      private _weight: number = 1,
      private _color: p5.Color = p.color(255, 255, 255),
      private _grid: Grid = grid
    ) {}

    static rect() {
      return new Rect();
    }

    x(x: number): Rect {
      this._x = this._grid.toGridX(x);
      return this;
    }

    y(y: number): Rect {
      this._y = this._grid.toGridY(y);
      return this;
    }

    w(w: number): Rect {
      this._w = this._grid.toGridW(w);
      return this;
    }

    h(h: number): Rect {
      this._h = this._grid.toGridH(h);
      return this;
    }

    weight(weight: number): Rect {
      this._weight = this._grid.toGridW(weight);
      return this;
    }

    color(color: p5.Color): Rect {
      this._color = color;
      return this;
    }

    draw(p: p5): void {
      p.stroke(this._color);
      p.strokeWeight(this._weight);
      p.noFill();
      p.rect(this._x, this._y, this._w, this._h);
    }
  }

  class CrossHair {
    private constructor(
      //
      private _x: number = 0,
      private _y: number = 0,
      private _radius: number = 1,
      private _weight: number = 1,
      private _color: p5.Color = p.color(255, 255, 255),
      private _grid: Grid = grid
    ) {}

    static crossHair() {
      return new CrossHair();
    }

    x(x: number): CrossHair {
      this._x = x;
      return this;
    }

    y(y: number): CrossHair {
      this._y = y;
      return this;
    }

    radius(radius: number): CrossHair {
      this._radius = radius;
      return this;
    }

    weight(weight: number): CrossHair {
      this._weight = weight;
      return this;
    }

    color(color: p5.Color): CrossHair {
      this._color = color;
      return this;
    }

    draw(p: p5): void {
      line() //
        .x1(this._x - this._radius)
        .y1(this._y)
        .x2(this._x + this._radius)
        .y2(this._y)
        .weight(this._weight)
        .color(this._color)
        .draw(p);
      line()
        .x1(this._x)
        .y1(this._y - this._radius)
        .x2(this._x)
        .y2(this._y + this._radius)
        .weight(this._weight)
        .color(this._color)
        .draw(p);
    }
  }

  const crossHair = () => CrossHair.crossHair();

  enum EpochLevel {
    day = 'day',
    hour = 'hour',
    minute = 'minute',
    second = 'second',
    millisecond = 'millisecond',
    subSecond = 'subSecond',
    transactionEvent = 'transactionEvent',
  }

  class EpochTime {
    protected constructor(
      //
      protected _value: number = 0,
      protected _level: EpochLevel = EpochLevel.day
    ) {}

    static epochTime() {
      return new EpochTime();
    }

    value(value: number): EpochTime {
      this._value = value;
      return this;
    }
    level(level: EpochLevel): EpochTime {
      this._level = level;
      return this;
    }
    toDay(): EpochTime {
      return this.toMillisecond().fromMsTo(EpochLevel.day);
    }
    toHour(): EpochTime {
      return this.toMillisecond().fromMsTo(EpochLevel.hour);
    }
    toMinute(): EpochTime {
      return this.toMillisecond().fromMsTo(EpochLevel.minute);
    }
    toSecond(): EpochTime {
      return this.toMillisecond().fromMsTo(EpochLevel.second);
    }
    toSubSecond(): EpochTime {
      return this.toMillisecond().fromMsTo(EpochLevel.subSecond);
    }
    toTransactionEvent(): EpochTime {
      return this.toMillisecond().fromMsTo(EpochLevel.transactionEvent);
    }
    private toMillisecond(): EpochTime {
      switch (this._level) {
        case EpochLevel.day:
          return new EpochTime(this._value * 24 * 60 * 60 * 1000, EpochLevel.millisecond);
        case EpochLevel.hour:
          return new EpochTime(this._value * 60 * 60 * 1000, EpochLevel.millisecond);
        case EpochLevel.minute:
          return new EpochTime(this._value * 60 * 1000, EpochLevel.millisecond);
        case EpochLevel.second:
          return new EpochTime(this._value * 1000, EpochLevel.millisecond);
        case EpochLevel.millisecond:
          return this;
        case EpochLevel.subSecond:
          return new EpochTime(this._value / 1000, EpochLevel.millisecond);
        case EpochLevel.transactionEvent:
          return new EpochTime(this._value, EpochLevel.millisecond);
      }
    }
    private fromMsTo(level: EpochLevel): EpochTime {
      switch (level) {
        case EpochLevel.day:
          return new EpochTime(this._value / 24 / 60 / 60 / 1000, EpochLevel.day);
        case EpochLevel.hour:
          return new EpochTime(this._value / 60 / 60 / 1000, EpochLevel.hour);
        case EpochLevel.minute:
          return new EpochTime(this._value / 60 / 1000, EpochLevel.minute);
        case EpochLevel.second:
          return new EpochTime(this._value / 1000, EpochLevel.second);
        case EpochLevel.millisecond:
          return this;
        case EpochLevel.subSecond:
          const slice: number = EpochTime.timeToSlice(this._value, 20);
          return new EpochTime(this._value * 1000 * 100 + slice, EpochLevel.subSecond);
        case EpochLevel.transactionEvent:
          return new EpochTime(this._value, EpochLevel.transactionEvent);
      }
    }
    private static timeToSlice(time: number, slice: number): number {
      const s = time.toLocaleString();
      return EpochTime.cyrb53(s) % slice;
    }
    // https://github.com/bryc/code/blob/master/jshash/experimental/cyrb53.js
    private static cyrb53(str: string, seed: number = 0): number {
      let h1: number = 0xdeadbeef ^ seed;
      let h2 = 0x41c6ce57 ^ seed;
      for (let i = 0; i < str.length; i++) {
        const ch = str.charCodeAt(i);
        h1 = Math.imul(h1 ^ ch, 2654435761);
        h2 = Math.imul(h2 ^ ch, 1597334677);
      }
      h1 = Math.imul(h1 ^ (h1 >>> 16), 2246822507) ^ Math.imul(h2 ^ (h2 >>> 13), 3266489909);
      h2 = Math.imul(h2 ^ (h2 >>> 16), 2246822507) ^ Math.imul(h1 ^ (h1 >>> 13), 3266489909);

      return 4294967296 * (2097151 & h2) + (h1 >>> 0);
    }
  }

  const epochTime = () => EpochTime.epochTime();

  class Payload {
    private constructor(
      //
      private _merchantId: string = '',
      private _payloadId: string = '',
      private _amount: number = 0
    ) {}

    static payload() {
      return new Payload();
    }

    merchantId(merchantId: string): Payload {
      this._merchantId = merchantId;
      return this;
    }

    payloadId(payloadId: string): Payload {
      this._payloadId = payloadId;
      return this;
    }

    amount(amount: number): Payload {
      this._amount = amount;
      return this;
    }
  }

  const payload = () => Payload.payload();

  class Interval {
    private constructor(
      //
      private _id: string = '',
      private _payload: Payload = Payload.payload(),
      private _epochTime: EpochTime = EpochTime.epochTime()
    ) {}

    static interval() {
      return new Interval('', payload(), epochTime());
    }

    id(id: string): Interval {
      this._id = id;
      return this;
    }

    amount(amount: number): Interval {
      this._payload.amount(amount);
      return this;
    }

    epochTime(epochTime: EpochTime): Interval {
      this._epochTime = epochTime;
      return this;
    }
  }

  const interval = () => Interval.interval();

  const drawBackgroundGrid = (p: p5) => {
    const bgColor = p.color(10, 20, 35);
    const gridColor = p.color(255, 255, 255, 200);

    p.background(bgColor);

    const xMax = 9;
    const yMax = p.round(p.max(12, p.windowHeight / grid.tickPixelWidth) / 2);
    const radius = 1 / 6;
    [...Array(xMax).keys()].forEach((x) => {
      [...Array(yMax).keys()].forEach((y) => {
        crossHair()
          .x(x * 2)
          .y(y * 2)
          .radius(radius)
          .weight(0.004)
          .color(gridColor)
          .draw(p);
      });
    });
    [...Array(xMax * 6).keys()].forEach((x) => {
      [...Array(yMax * 6).keys()].forEach((y) => {
        point()
          .x(x / 3 + 1 / 6)
          .y(y / 3 + 1 / 6)
          .weight(0.008)
          .color(gridColor)
          .draw(p);
      });
    });
  };

  const drawMerchantAndPayment = (p: p5) => {
    const border = 0.075;
    const boxAccent = p.color(219, 167, 158);
    const boxColor = p.color(200, 200, 200, 50);
    const boxColorValue = p.color(200, 200, 200, 25);
    const textColor = p.color(0, 203, 191);

    label() //
      .text('Merchant Id')
      .x(0.2)
      .y(0.2)
      .w(1)
      .h(0.3)
      .border(border)
      .bgColor(boxColor)
      .textColor(textColor)
      .draw(p);

    label() //
      .text('00074563916832')
      .textAlign(p.RIGHT)
      .x(1.2)
      .y(0.2)
      .w(1.5)
      .h(0.3)
      .border(border)
      .bgColor(boxColorValue)
      .draw(p);

    label() //
      .text('Payment Id')
      .x(2.8)
      .y(0.2)
      .w(1)
      .h(0.3)
      .border(border)
      .bgColor(boxColor)
      .textColor(textColor)
      .draw(p);

    label() //
      .text('00000001')
      .textAlign(p.RIGHT)
      .x(3.8)
      .y(0.2)
      .w(1)
      .h(0.3)
      .border(border)
      .bgColor(boxColorValue)
      .draw(p);

    label() //
      .text('Days')
      .x(1)
      .y(1)
      .w(1)
      .h(0.3)
      .border(border)
      .bgColor(boxColor)
      .draw(p);
  };

  const drawDays = (p: p5) => {};

  p.setup = () => {
    console.log('dashboard - setup initialized, P5 is running');

    p.createCanvas(p.windowWidth, p.windowHeight);
  };

  p.draw = () => {
    p.clear(0, 0, 0, 0);

    drawBackgroundGrid(p);
    drawMerchantAndPayment(p);
    drawDays(p);
  };

  p.windowResized = (event) => {
    console.log(`dashboard - window resized: ${event}`);

    p.resizeCanvas(p.windowWidth, p.windowHeight);
    grid = Grid.resize(p);
  };

  p.mouseClicked = (event: PointerEvent) => {
    console.log(`dashboard - mouse clicked: x:${event.x}, y:${event.y}`);
  };

  class ToTime {
    private constructor(
      //
      private _time: number
    ) {}

    static fromZero() {
      return new ToTime(0);
    }

    static fromNow(): ToTime {
      return new ToTime(Date.now());
    }

    static fromDay(day: number): ToTime {
      return new ToTime(day * 24 * 60 * 60 * 1000);
    }

    static fromHour(hour: number): ToTime {
      return new ToTime(hour * 60 * 60 * 1000);
    }

    static fromMinute(minute: number): ToTime {
      return new ToTime(minute * 60 * 1000);
    }

    static fromSecond(second: number): ToTime {
      return new ToTime(second * 1000);
    }

    static fromMillisecond(millisecond: number): ToTime {
      return new ToTime(millisecond);
    }

    toDay(): number {
      return this._time / (24 * 60 * 60 * 1000);
    }

    toHour(): number {
      return this._time / (60 * 60 * 1000);
    }

    toMinute(): number {
      return this._time / (60 * 1000);
    }

    toSecond(): number {
      return this._time / 1000;
    }

    toMillisecond(): number {
      return this._time;
    }
  }
};

new p5(aggregatorDashboard);

export {};
