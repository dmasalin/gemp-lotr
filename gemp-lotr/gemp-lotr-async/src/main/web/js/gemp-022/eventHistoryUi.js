/**
 * Browser for completed events of ONE kind ("league" or "tournament"), a month at a time.  The Events tab has one
 * panel under Current Leagues (kind "league") and one under Current Tournaments (kind "tournament").
 *
 *   new EventHistoryUI(hall.comm, $("#leagueHistory"), "league", leagueUI).show();
 *   new EventHistoryUI(hall.comm, $("#tournamentHistory"), "tournament", tourneyUI).show();
 *
 * The header mirrors the calendar's (.calendar-header): "<", the month title, ">", and a "Load" button in the place of
 * the calendar's "Today".  The title starts on the current UTC month and nothing is requested when the page loads.
 * Until "Load" is pressed the arrows and the month dropdown only change the title; "Load" fetches the titled month and
 * then hides itself for good, after which every arrow press / dropdown choice fetches straight away.
 *
 * Both the arrows and the dropdown work from the list of months the server reports as having completed events of this
 * kind (fetched once, on the first arrow press or dropdown opening): the arrows jump to the previous / next such
 * month, skipping empty ones, and are disabled when there is none in that direction.  If that list cannot be fetched
 * the arrows fall back to stepping one calendar month, so the control never becomes unusable.
 *
 * Data (see communication.js):
 *   comm.getEventHistoryMonths(kind)        -> {kind, months:["2026-09","2026-08",...]}   newest first
 *   comm.getEventHistory(month, kind)       -> {month, kind, isAdmin, events:[...]}
 *   comm.getLeague(id) / comm.getTournament(id) -> the XML the live displays render
 *
 * "Details" on a row expands a row beneath it and renders the league / tournament with the SAME renderer the live
 * display uses (LeagueResultsUI.loadedLeague into a target, TournamentResultsUI.loadedTournament), so a finished
 * league shows its full serie breakdown and results tabs and a finished tournament its standings with deck links.
 * The row slides open and shut (and eases to its new height when the fetched details replace the loading line); the
 * drawer mechanism is EventDrawer (eventDrawer.js), shared with the Current Leagues / Tournaments lists.
 *
 * Admin extras are gated only on the response's isAdmin flag (never hall.userInfo, which is filled in
 * asynchronously and does not exist on every page).  Every server-provided string in the table goes into the DOM
 * through .text()/.attr().
 */
var EventHistoryUI = Class.extend({
    comm: null,
    container: null,
    kind: null,            // "league" | "tournament"
    resultsUI: null,       // LeagueResultsUI / TournamentResultsUI used to render a row's details
    options: null,
    uid: 0,

    year: 0,               // the month in the title
    month: 0,              // 1-12
    live: false,           // false until "Load" is pressed; afterwards navigation fetches immediately
    requested: null,       // "yyyy-MM" most recently requested; responses for any other month are discarded
    monthsState: "idle",   // idle | loading | ready | error   (the months-with-events list)
    monthsError: null,
    monthsWaiting: null,   // callbacks queued on the in-flight months request; each gets the list, or null on failure
    dropdownOpen: false,

    root: null,
    prevBut: null,
    nextBut: null,
    loadBut: null,
    title: null,
    titleText: null,
    dropdown: null,
    status: null,
    body: null,

    init: function (comm, container, kind, resultsUI, options) {
        this.comm = comm;
        this.container = container;
        this.kind = (kind == "tournament") ? "tournament" : "league";
        this.resultsUI = resultsUI;
        this.options = options || {};
        this.uid = ++EventHistoryUI.instances;
        var now = EventHistoryUI.currentMonth();
        this.year = now.year;
        this.month = now.month;
    },

    // Builds the panel.  No request is made here.
    show: function () {
        var that = this;
        this.container.empty();

        this.root = $("<div class='event-history'></div>").addClass("event-history-" + this.kind);
        this.root.append($("<div class='event-history-heading'></div>")
            .text(this.kind == "league" ? "Completed leagues" : "Completed tournaments"));

        var header = $("<div class='calendar-header event-history-header'></div>");
        this.prevBut = $("<button type='button' title='Previous month with events'>&lt;</button>").button()
            .click(function () { that.jump(-1); });
        this.nextBut = $("<button type='button' title='Next month with events'>&gt;</button>").button()
            .click(function () { that.jump(1); });
        this.loadBut = $("<button type='button' class='event-history-load'>Load</button>").button()
            .click(function () { that.firstLoad(); });

        var titleWrap = $("<span class='event-history-title-wrap'></span>");
        this.title = $("<span class='calendar-title event-history-month-title' role='button' tabindex='0'"
            + " aria-haspopup='true' aria-expanded='false' title='Choose a month'></span>");
        this.titleText = $("<span class='event-history-month-text'></span>");
        this.title.append(this.titleText).append("<span class='event-history-caret' aria-hidden='true'>&#9662;</span>");
        this.title.click(function () { that.toggleDropdown(); });
        this.title.keydown(function (e) {
            if (e.key == "Enter" || e.key == " ") {
                e.preventDefault();
                that.toggleDropdown();
            }
        });
        this.dropdown = $("<div class='event-history-dropdown'></div>").hide();
        titleWrap.append(this.title).append(this.dropdown);

        header.append(this.prevBut).append(titleWrap).append(this.nextBut).append(this.loadBut);
        this.root.append(header);

        this.status = $("<div class='event-history-status'></div>");
        this.root.append(this.status);
        this.body = $("<div class='event-history-body'></div>");
        this.root.append(this.body);

        this.container.append(this.root);

        this.sync();
        this.setStatus("Choose a month with the arrows or by clicking its name, then press Load. Nothing is loaded until you do.", "hint");
        return this;
    },

    // ---- navigation ----

    monthKey: function () {
        return EventHistoryUI.monthKey(this.year, this.month);
    },

    isAtCurrentMonth: function () {
        var now = EventHistoryUI.currentMonth();
        return this.year * 12 + this.month >= now.year * 12 + now.month;
    },

    // Moves to the nearest month with events before (delta < 0) or after (delta > 0) the one in the title.  The
    // months list is fetched on the first press; while it is in flight the arrows are disabled.
    jump: function (delta) {
        var that = this;
        var months = EventHistoryUI.monthsCache[this.kind];
        if (months != null) {
            var target = EventHistoryUI.neighbourMonth(months, this.monthKey(), delta);
            if (target != null)
                this.setMonth(target);
            return;
        }
        this.loadMonths(function (list) {
            if (list == null)
                that.step(delta);   // the list is unavailable: fall back to plain calendar stepping
            else
                that.jump(delta);
        });
    },

    // One calendar month, used only when the months list could not be fetched.
    step: function (delta) {
        if (delta > 0 && this.isAtCurrentMonth())
            return;
        var index = this.year * 12 + (this.month - 1) + delta;
        this.year = Math.floor(index / 12);
        this.month = index % 12 + 1;
        this.navigated();
    },

    setMonth: function (key) {
        var parts = String(key).split("-");
        var year = parseInt(parts[0], 10);
        var month = parseInt(parts[1], 10);
        if (isNaN(year) || isNaN(month) || month < 1 || month > 12)
            return;
        this.year = year;
        this.month = month;
        this.navigated();
    },

    navigated: function () {
        this.sync();
        if (this.live)
            this.load();
    },

    sync: function () {
        this.titleText.text(EventHistoryUI.monthLabel(this.monthKey()));
        var months = EventHistoryUI.monthsCache[this.kind];
        var prevOff, nextOff;
        if (this.monthsState == "loading") {
            prevOff = nextOff = true;
        } else if (months != null) {
            prevOff = EventHistoryUI.neighbourMonth(months, this.monthKey(), -1) == null;
            nextOff = EventHistoryUI.neighbourMonth(months, this.monthKey(), 1) == null;
        } else {
            // not fetched yet (or the fetch failed): stay usable; only the future is ruled out
            prevOff = false;
            nextOff = this.isAtCurrentMonth();
        }
        this.prevBut.button("option", "disabled", prevOff);
        this.nextBut.button("option", "disabled", nextOff);
        if (this.dropdownOpen)
            this.renderDropdown();
    },

    firstLoad: function () {
        this.live = true;
        this.loadBut.hide();
        this.load();
    },

    // ---- month dropdown ----

    toggleDropdown: function () {
        if (this.dropdownOpen)
            this.closeDropdown();
        else
            this.openDropdown();
    },

    openDropdown: function () {
        var that = this;
        this.dropdownOpen = true;
        this.title.attr("aria-expanded", "true").addClass("open");
        this.dropdown.show();
        var ns = ".eventHistory" + this.uid;
        $(document).on("mousedown" + ns, function (e) {
            if ($(e.target).closest(that.title.parent()).length == 0)
                that.closeDropdown();
        });
        $(document).on("keydown" + ns, function (e) {
            if (e.key == "Escape" || e.key == "Esc") {
                that.closeDropdown();
                that.title.focus();
            }
        });
        if (EventHistoryUI.monthsCache[this.kind] != null)
            this.monthsState = "ready";
        else
            this.loadMonths();
        this.renderDropdown();
    },

    closeDropdown: function () {
        this.dropdownOpen = false;
        this.title.attr("aria-expanded", "false").removeClass("open");
        this.dropdown.hide();
        $(document).off(".eventHistory" + this.uid);
    },

    // Fetches the months-with-events list (once; concurrent callers share the request).  `then`, if given, is called
    // with the list, or with null if it could not be fetched.
    loadMonths: function (then) {
        var that = this;
        var kind = this.kind;
        var cached = EventHistoryUI.monthsCache[kind];
        if (cached != null) {
            this.monthsState = "ready";
            if (then)
                then(cached);
            return;
        }
        if (this.monthsState == "loading") {
            if (then)
                this.monthsWaiting.push(then);
            return;
        }
        this.monthsState = "loading";
        this.monthsError = null;
        this.monthsWaiting = then ? [then] : [];
        this.sync();

        var finish = function (list) {
            var waiting = that.monthsWaiting || [];
            that.monthsWaiting = null;
            that.sync();
            for (var i = 0; i < waiting.length; i++)
                waiting[i](list);
        };
        this.comm.getEventHistoryMonths(kind,
            function (json) {
                var list = (json && json.months) ? json.months : [];
                EventHistoryUI.monthsCache[kind] = list;
                that.monthsState = "ready";
                finish(list);
            },
            this.errorMap(function (message) {
                that.monthsState = "error";
                that.monthsError = message;
                finish(null);
            }));
    },

    renderDropdown: function () {
        var that = this;
        var dd = this.dropdown;
        dd.empty();

        if (this.monthsState == "loading") {
            dd.append($("<div class='event-history-dd-status event-history-loading'></div>").text("Loading months…"));
            return;
        }
        if (this.monthsState == "error") {
            dd.append($("<div class='event-history-dd-status event-history-error'></div>")
                .text("Could not load the list of months. " + (this.monthsError || "")));
            dd.append($("<button type='button'>Retry</button>").button().click(function () {
                that.monthsState = "idle";
                that.loadMonths();
            }));
            return;
        }

        var months = EventHistoryUI.monthsCache[this.kind] || [];
        if (months.length == 0) {
            dd.append($("<div class='event-history-dd-status event-history-empty'></div>")
                .text("No completed " + this.kindPlural() + " have been recorded yet."));
            return;
        }

        var current = this.monthKey();
        var group = null, groupYear = null;
        for (var i = 0; i < months.length; i++) {
            var key = String(months[i]);
            var year = key.split("-")[0];
            if (year !== groupYear) {
                groupYear = year;
                var yearDiv = $("<div class='event-history-dd-year'></div>");
                yearDiv.append($("<div class='event-history-dd-year-label'></div>").text(year));
                group = $("<div class='event-history-dd-months'></div>");
                yearDiv.append(group);
                dd.append(yearDiv);
            }
            var item = $("<button type='button' class='event-history-dd-month'></button>")
                .attr("data-month", key)
                .attr("title", EventHistoryUI.monthLabel(key))
                .text(EventHistoryUI.monthName(key));
            if (key == current)
                item.addClass("selected");
            item.click((function (value) {
                return function () {
                    that.closeDropdown();
                    that.title.focus();
                    that.setMonth(value);
                };
            })(key));
            group.append(item);
        }
    },

    // ---- one month ----

    load: function () {
        var that = this;
        var month = this.monthKey();
        var kind = this.kind;
        var cacheKey = kind + "|" + month;
        this.requested = month;

        var cached = EventHistoryUI.cache[cacheKey];
        if (cached != null) {
            this.renderMonth(cached, month);
            return;
        }
        this.body.empty();
        this.setStatus("Loading " + EventHistoryUI.monthLabel(month) + "…", "loading");
        this.comm.getEventHistory(month, kind,
            function (json) {
                EventHistoryUI.cache[cacheKey] = json;
                // a slow response for a month the viewer has already navigated away from must not overwrite the view
                if (that.requested == month)
                    that.renderMonth(json, month);
            },
            this.errorMap(function (message) {
                if (that.requested != month)
                    return;
                that.body.empty();
                that.setStatus("Could not load " + EventHistoryUI.monthLabel(month) + ". " + message, "error");
                that.status.append(" ").append($("<button type='button'>Retry</button>").button().click(function () {
                    if (that.requested == month)
                        that.load();
                }));
            }));
    },

    renderMonth: function (json, month) {
        var events = (json && json.events) ? json.events : [];
        var isAdmin = !!(json && json.isAdmin);
        var label = EventHistoryUI.monthLabel(month);

        this.body.empty();

        if (events.length == 0) {
            this.setStatus("No completed " + this.kindPlural() + " in " + label + ".", "empty");
            return;
        }
        this.setStatus(label + ": " + events.length + " completed "
            + (events.length == 1 ? this.kind : this.kindPlural()) + ".", "loaded");

        var columns = this.kind == "league"
            ? ["Name", "Dates", "Format", "Players", ""]
            : ["Name", "Date", "Format", "Players", "Rounds", ""];
        if (isAdmin)
            columns.push("Admin");

        var table = $("<table class='event-history-table'></table>");
        var headRow = $("<tr></tr>");
        for (var c = 0; c < columns.length; c++)
            headRow.append($("<th></th>").text(columns[c]));
        table.append($("<thead></thead>").append(headRow));

        var tbody = $("<tbody></tbody>");
        for (var i = 0; i < events.length; i++)
            tbody.append(this.renderRow(events[i] || {}, isAdmin, columns.length));
        table.append(tbody);
        this.body.append(table);
    },

    renderRow: function (event, isAdmin, columnCount) {
        var row = $("<tr class='event-history-row'></tr>");
        row.append($("<td class='event-history-name'></td>").text(EventHistoryUI.orDash(event.name)));
        if (this.kind == "league") {
            var start = EventHistoryUI.orDash(event.startDate);
            var end = EventHistoryUI.orDash(event.endDate);
            row.append($("<td class='event-history-dates'></td>").text(start + " – " + end));
        } else {
            row.append($("<td class='event-history-dates'></td>").text(EventHistoryUI.orDash(event.startDate)));
        }
        row.append($("<td></td>").text(EventHistoryUI.orDash(event.format)));
        row.append($("<td class='event-history-number'></td>").text(EventHistoryUI.orDash(event.playerCount)));
        if (this.kind == "tournament")
            row.append($("<td class='event-history-number'></td>").text(EventHistoryUI.orDash(event.rounds)));

        var detailCell = $("<td class='event-history-detail-cell'></td>");
        if (event.id != null)
            detailCell.append(this.renderDetailsButton(row, String(event.id), columnCount));
        row.append(detailCell);

        if (isAdmin)
            row.append(this.renderAdminCell(event));
        return row;
    },

    // Details expand into a row directly beneath the event's row, as an EventDrawer (eventDrawer.js).  The first
    // expansion fetches and renders; later toggles only slide the row open / shut.  A failed fetch is retried on the
    // next expansion.
    renderDetailsButton: function (row, id, columnCount) {
        var that = this;
        var button = $("<button type='button' class='event-history-details' aria-expanded='false'>Details</button>").button();
        var drawer = new EventDrawer({
            header: row,
            button: button,
            closedLabel: "Details",
            openLabel: "Hide details",
            slideClass: "event-history-detail-slide",
            paneClass: "event-history-detail",
            loadingClass: "event-history-loading",
            errorClass: "event-history-error",
            mount: function (slide) {
                var detailRow = $("<tr class='event-history-detail-row'></tr>")
                    .append($("<td></td>").attr("colspan", columnCount).append(slide));
                row.after(detailRow);
                return detailRow;
            },
            load: function (drawer, content) {
                that.loadDetails(id, drawer, content);
            }
        });
        button.click(function () { drawer.toggle(); });
        return button;
    },

    loadDetails: function (id, drawer, content) {
        var that = this;
        var success = function (xml) {
            drawer.settle(content, function () {
                return that.renderDetails(xml, content);
            });
        };
        var failure = this.errorMap(function (message, httpStatus) {
            drawer.fail(content, "Could not load the details (HTTP " + httpStatus + "). " + message);
        });

        if (this.kind == "league")
            this.comm.getLeague(id, success, failure);
        else
            this.comm.getTournament(id, success, failure);
    },

    // Returns false when the details could not be displayed.
    renderDetails: function (xml, content) {
        var that = this;
        content.empty();
        try {
            if (that.kind == "league")
                that.resultsUI.loadedLeague(xml, content,
                    {idPrefix: "eventHistory" + (++EventHistoryUI.detailRenders) + "-", membership: false});
            else
                that.resultsUI.loadedTournament(xml, content);
        } catch (e) {
            content.empty();
            content.append($("<div class='event-history-error'></div>").text("Could not display the details."));
            return false;
        }
        if (content.children().length == 0)
            content.append($("<i></i>").text("No details are available for this " + that.kind + "."));
        return true;
    },

    // A link with a url is a real anchor (the tournament report, whose text is the tournament id); one without is
    // plain text (the league code) that a click selects whole.
    renderAdminCell: function (event) {
        var cell = $("<td class='event-history-admin'></td>");
        var links = event.adminLinks;
        var any = false;
        for (var i = 0; links != null && i < links.length; i++) {
            var link = links[i];
            if (link == null)
                continue;
            var label = link.label == null ? "" : String(link.label);
            var text = link.text != null ? String(link.text) : label;
            var url = EventHistoryUI.safeUrl(link.url);
            var item = $("<span class='event-history-admin-item'></span>");
            if (url != null) {
                item.append($("<a class='event-history-admin-link' target='_blank'></a>")
                    .attr("href", url)
                    .attr("title", label ? label + ": " + url : url)
                    .text(text || url));
            } else if (text) {
                item.append($("<span class='event-history-admin-text'></span>")
                    .attr("title", (label ? label + " - " : "") + "click to select")
                    .text(text)
                    .click(function () { EventDrawer.selectText(this); }));
            } else {
                continue;
            }
            cell.append(item);
            any = true;
        }
        if (!any)
            cell.append($("<span class='event-history-admin-none'></span>").text("—"));
        return cell;
    },

    // ---- helpers ----

    kindPlural: function () {
        return this.kind == "league" ? "leagues" : "tournaments";
    },

    setStatus: function (text, state) {
        this.status.attr("class", "event-history-status event-history-" + state).text(text);
    },

    // Every status (0 and 400-599) is mapped, so no failure here reaches communication.js's global failure popup;
    // report(message, status) writes it inline instead.
    errorMap: function (report) {
        return EventDrawer.errorMap(report);
    }
});

EventHistoryUI.instances = 0;
EventHistoryUI.detailRenders = 0;               // makes each rendered league's tab ids unique on the page
EventHistoryUI.cache = {};                      // "kind|yyyy-MM" -> month response
EventHistoryUI.monthsCache = {league: null, tournament: null};   // kind -> months list, fetched at most once

EventHistoryUI.monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September",
    "October", "November", "December"];

EventHistoryUI.currentMonth = function () {
    var now = new Date();
    return {year: now.getUTCFullYear(), month: now.getUTCMonth() + 1};
};

EventHistoryUI.monthKey = function (year, month) {
    return year + "-" + (month < 10 ? "0" : "") + month;
};

EventHistoryUI.monthIndex = function (key) {
    var parts = String(key).split("-");
    if (parts.length < 2)
        return -1;
    var index = parseInt(parts[1], 10) - 1;
    return (isNaN(index) || index < 0 || index > 11) ? -1 : index;
};

// "2026-03" -> "March"; anything unexpected is echoed back.
EventHistoryUI.monthName = function (key) {
    var index = EventHistoryUI.monthIndex(key);
    return index < 0 ? String(key) : EventHistoryUI.monthNames[index];
};

// "2026-03" -> "March 2026"; anything unexpected is echoed back rather than turned into "undefined NaN".
EventHistoryUI.monthLabel = function (key) {
    if (key == null)
        return "";
    var index = EventHistoryUI.monthIndex(key);
    return index < 0 ? String(key) : EventHistoryUI.monthNames[index] + " " + String(key).split("-")[0];
};

EventHistoryUI.orDash = function (value) {
    return (value == null || value === "") ? "—" : String(value);
};

// Only same-origin absolute paths and http(s) URLs become hrefs; anything else (javascript:, data:, a
// protocol-relative //host) is refused so a bad server value cannot turn into script.
EventHistoryUI.safeUrl = function (url) {
    if (url == null)
        return null;
    var value = String(url);
    if (/^\/(?!\/)/.test(value) || /^https?:\/\//i.test(value))
        return value;
    return null;
};

EventHistoryUI.selectText = function (node) {
    EventDrawer.selectText(node);
};

// The nearest entry of `months` ("yyyy-MM", any order) strictly before (delta < 0) or after (delta > 0) `current`,
// or null if there is none in that direction.  "yyyy-MM" strings compare correctly as plain strings.
EventHistoryUI.neighbourMonth = function (months, current, delta) {
    var best = null;
    for (var i = 0; i < months.length; i++) {
        var key = String(months[i]);
        if (delta < 0 ? key < current : key > current) {
            if (best == null || (delta < 0 ? key > best : key < best))
                best = key;
        }
    }
    return best;
};

// The slide / resize animation now lives in eventDrawer.js (shared with the Current Leagues / Tournaments lists and
// the calendar preview); these names are kept for any caller of the old helpers.
EventHistoryUI.duration = function () {
    return EventDrawer.duration();
};

EventHistoryUI.slide = function (wrapper, open, done) {
    EventDrawer.slide(wrapper, open, done);
};

EventHistoryUI.resize = function (wrapper, open, mutate) {
    EventDrawer.resize(wrapper, open, mutate);
};
