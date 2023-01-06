"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const aggregatorDashboard = (p) => {
    class Grid {
        constructor(
        //
        border, tickPixelWidth) {
            this.border = border;
            this.tickPixelWidth = tickPixelWidth;
        }
        static resize(p) {
            const border = 20;
            const tickWidth = (p.windowWidth - 2 * border) / 16; // aspect ratio 16:9
            const tickHeight = (p.windowHeight - 2 * border) / 9;
            return new Grid(border, Math.min(tickWidth, tickHeight));
        }
        toGridX(x) {
            return this.border + x * this.tickPixelWidth;
        }
        toGridY(y) {
            return this.border + y * this.tickPixelWidth;
        }
        toGridW(w) {
            return w * this.tickPixelWidth;
        }
        toGridH(h) {
            return h * this.tickPixelWidth;
        }
    }
    let grid = Grid.resize(p);
    class Label {
        constructor(
        //
        _text = '', _x = 0, _y = 0, _w = 0, _h = 0, _border = 0, _bgColor = p.color(0, 0, 0, 0), _textColor = p.color(255, 255, 255), _textAlign = ['left', 'alphabetic'], _grid = grid) {
            this._text = _text;
            this._x = _x;
            this._y = _y;
            this._w = _w;
            this._h = _h;
            this._border = _border;
            this._bgColor = _bgColor;
            this._textColor = _textColor;
            this._textAlign = _textAlign;
            this._grid = _grid;
        }
        static label() {
            return new Label();
        }
        text(text) {
            this._text = text;
            return this;
        }
        x(x) {
            this._x = this._grid.toGridX(x);
            return this;
        }
        y(y) {
            this._y = this._grid.toGridY(y);
            return this;
        }
        w(w) {
            this._w = this._grid.toGridW(w);
            return this;
        }
        h(h) {
            this._h = this._grid.toGridH(h);
            return this;
        }
        border(border) {
            this._border = this._grid.toGridW(border);
            return this;
        }
        pixelX(x) {
            this._x = x;
            return this;
        }
        pixelY(y) {
            this._y = y;
            return this;
        }
        pixelW(w) {
            this._w = w;
            return this;
        }
        pixelH(h) {
            this._h = h;
            return this;
        }
        bgColor(bgColor) {
            this._bgColor = bgColor;
            return this;
        }
        textColor(textColor) {
            this._textColor = textColor;
            return this;
        }
        textAlign(horizontal, vertical) {
            this._textAlign = [horizontal, vertical !== null && vertical !== void 0 ? vertical : 'alphabetic'];
            return this;
        }
        draw(p) {
            p.stroke(0);
            p.fill(this._bgColor);
            p.rect(this._x, this._y, this._w, this._h);
            p.fill(this._textColor);
            p.textSize(this._h - 2 * this._border);
            p.textAlign(this._textAlign[0], this._textAlign[1]);
            p.text(this._text, this._x + this._border, this._y + this._border, this._w - 2 * this._border, this._h - 2 * this._border);
        }
    }
    class Labels {
        constructor(
        //
        _labels = [], _indexToRow = (i) => i, _indexToCol = (i) => i) {
            this._labels = _labels;
            this._indexToRow = _indexToRow;
            this._indexToCol = _indexToCol;
        }
        static labels() {
            return new Labels();
        }
        indexToRow(callback) {
            this._indexToRow = callback;
            return this;
        }
        indexToCol(callback) {
            this._indexToCol = callback;
            return this;
        }
        add(label) {
            this._labels.push(label);
            return this;
        }
        draw(p) {
            this._labels.forEach((label) => label.draw(p));
        }
    }
    class DataLabel extends Label {
        constructor(
        //
        _label = label(), _hour = 0) {
            super();
            this._label = _label;
            this._hour = _hour;
        }
        static hourLabel(hour) {
            return new DataLabel();
        }
        draw(p) {
            this.x(this._hour).y(-1).w(1).h(1);
            super.draw(p);
        }
    }
    const label = () => Label.label();
    class Point {
        constructor(
        //
        _x = 0, _y = 0, _weight = 1, _color = p.color(255, 255, 255), _grid = grid) {
            this._x = _x;
            this._y = _y;
            this._weight = _weight;
            this._color = _color;
            this._grid = _grid;
        }
        static point() {
            return new Point();
        }
        x(x) {
            this._x = this._grid.toGridX(x);
            return this;
        }
        y(y) {
            this._y = this._grid.toGridY(y);
            return this;
        }
        weight(weight) {
            this._weight = this._grid.toGridW(weight);
            return this;
        }
        color(color) {
            this._color = color;
            return this;
        }
        draw(p) {
            p.stroke(this._color);
            p.strokeWeight(this._weight);
            p.point(this._x, this._y);
        }
    }
    const point = () => Point.point();
    class Line {
        constructor(
        //
        _x1 = 0, _y1 = 0, _x2 = 0, _y2 = 0, _weight = 1, _color = p.color(255, 255, 255), _grid = grid) {
            this._x1 = _x1;
            this._y1 = _y1;
            this._x2 = _x2;
            this._y2 = _y2;
            this._weight = _weight;
            this._color = _color;
            this._grid = _grid;
        }
        static line() {
            return new Line();
        }
        x1(x1) {
            this._x1 = this._grid.toGridX(x1);
            return this;
        }
        y1(y1) {
            this._y1 = this._grid.toGridY(y1);
            return this;
        }
        x2(x2) {
            this._x2 = this._grid.toGridX(x2);
            return this;
        }
        y2(y2) {
            this._y2 = this._grid.toGridY(y2);
            return this;
        }
        weight(weight) {
            this._weight = this._grid.toGridW(weight);
            return this;
        }
        color(color) {
            this._color = color;
            return this;
        }
        draw(p) {
            p.stroke(this._color);
            p.strokeWeight(this._weight);
            p.line(this._x1, this._y1, this._x2, this._y2);
        }
    }
    const line = () => Line.line();
    class Rect {
        constructor(
        //
        _x = 0, _y = 0, _w = 0, _h = 0, _weight = 1, _color = p.color(255, 255, 255), _grid = grid) {
            this._x = _x;
            this._y = _y;
            this._w = _w;
            this._h = _h;
            this._weight = _weight;
            this._color = _color;
            this._grid = _grid;
        }
        static rect() {
            return new Rect();
        }
        x(x) {
            this._x = this._grid.toGridX(x);
            return this;
        }
        y(y) {
            this._y = this._grid.toGridY(y);
            return this;
        }
        w(w) {
            this._w = this._grid.toGridW(w);
            return this;
        }
        h(h) {
            this._h = this._grid.toGridH(h);
            return this;
        }
        weight(weight) {
            this._weight = this._grid.toGridW(weight);
            return this;
        }
        color(color) {
            this._color = color;
            return this;
        }
        draw(p) {
            p.stroke(this._color);
            p.strokeWeight(this._weight);
            p.noFill();
            p.rect(this._x, this._y, this._w, this._h);
        }
    }
    class CrossHair {
        constructor(
        //
        _x = 0, _y = 0, _radius = 1, _weight = 1, _color = p.color(255, 255, 255), _grid = grid) {
            this._x = _x;
            this._y = _y;
            this._radius = _radius;
            this._weight = _weight;
            this._color = _color;
            this._grid = _grid;
        }
        static crossHair() {
            return new CrossHair();
        }
        x(x) {
            this._x = x;
            return this;
        }
        y(y) {
            this._y = y;
            return this;
        }
        radius(radius) {
            this._radius = radius;
            return this;
        }
        weight(weight) {
            this._weight = weight;
            return this;
        }
        color(color) {
            this._color = color;
            return this;
        }
        draw(p) {
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
    let TimeLevel;
    (function (TimeLevel) {
        TimeLevel["day"] = "day";
        TimeLevel["hour"] = "hour";
        TimeLevel["minute"] = "minute";
        TimeLevel["second"] = "second";
        TimeLevel["subSecond"] = "subSecond";
        TimeLevel["aggregation"] = "aggregation";
    })(TimeLevel || (TimeLevel = {}));
    class Aggregator {
        constructor(
        //
        _id, _amount = 0, _epochTimeMs = 0, _epochLevel = TimeLevel.day) {
            this._id = _id;
            this._amount = _amount;
            this._epochTimeMs = _epochTimeMs;
            this._epochLevel = _epochLevel;
        }
    }
    const drawBackground = (p) => {
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
    const drawDashboard = (p) => {
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
    const drawDays = (p) => { };
    p.setup = () => {
        console.log('dashboard - setup initialized, P5 is running');
        p.createCanvas(p.windowWidth, p.windowHeight);
    };
    p.draw = () => {
        p.clear(0, 0, 0, 0);
        drawBackground(p);
        drawDashboard(p);
        drawDays(p);
    };
    p.windowResized = (event) => {
        console.log(`dashboard - window resized: ${event}`);
        p.resizeCanvas(p.windowWidth, p.windowHeight);
        grid = Grid.resize(p);
    };
    p.mouseClicked = (event) => {
        console.log(`dashboard - mouse clicked: x:${event.x}, y:${event.y}`);
    };
};
new p5(aggregatorDashboard);
//# sourceMappingURL=dashboard.js.map