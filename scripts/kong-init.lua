local kong = require "kong"

local function setup()
    local routes = {
        {
            name = "erp-iam",
            paths = { "/iam/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-pdm",
            paths = { "/pdm/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-som",
            paths = { "/som/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-ads",
            paths = { "/ads/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-oms",
            paths = { "/oms/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-scm",
            paths = { "/scm/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-wms",
            paths = { "/wms/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-fba",
            paths = { "/fba/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-tms",
            paths = { "/tms/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-crm",
            paths = { "/crm/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-fms",
            paths = { "/fms/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-bi",
            paths = { "/bi/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-sys",
            paths = { "/sys/api" },
            service_url = "http://erp-gateway:8080"
        },
        {
            name = "erp-dashboard",
            paths = { "/dashboard/api" },
            service_url = "http://erp-gateway:8080"
        }
    }

    for _, route_config in ipairs(routes) do
        kong.db.services:insert({
            name = route_config.name .. "-service",
            url = route_config.service_url
        })

        kong.db.routes:insert({
            name = route_config.name .. "-route",
            service = { id = kong.db.services:select_by_name(route_config.name .. "-service").id },
            paths = route_config.paths
        })
    end

    kong.db.plugins:insert({
        name = "rate-limiting",
        config = {
            minute = 100,
            hour = 1000,
            policy = "local"
        }
    })

    kong.log.info("Kong initialization completed: " .. #routes .. " routes configured")
end

return {
    ["kong-init"] = {
        schema = {},
        handler = setup
    }
}
